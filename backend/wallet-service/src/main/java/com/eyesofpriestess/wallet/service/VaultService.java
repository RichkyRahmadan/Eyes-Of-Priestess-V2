package com.eyesofpriestess.wallet.service;

import com.eyesofpriestess.wallet.client.AuthClient;
import com.eyesofpriestess.wallet.dto.request.*;
import com.eyesofpriestess.wallet.dto.response.*;
import com.eyesofpriestess.wallet.entity.*;
import com.eyesofpriestess.wallet.event.OfferingAcceptedEvent;
import com.eyesofpriestess.wallet.exception.VaultException;
import com.eyesofpriestess.wallet.repository.*;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

/**
 * VaultService — Core business engine for Vault Sanctum (Treasury & Payments).
 *
 * Implements:
 * - Treasury lifecycle (Auto-creation on first access)
 * - Xendit Top-Up (Offering creation & webhook processing)
 * - Xendit Bank Withdrawal (Disbursement)
 * - P2P Tithing Transfer (Balance transfer between Pilgrims)
 * - Escrow Management (Seal Hold, Release, Refund for Covenant Sanctum)
 * - Chronicle audit logging
 */
@ApplicationScoped
public class VaultService {

    private static final Logger LOG = Logger.getLogger(VaultService.class);

    @Inject TreasuryRepository treasuryRepo;
    @Inject OfferingRepository offeringRepo;
    @Inject WithdrawalRepository withdrawalRepo;
    @Inject ChronicleRepository chronicleRepo;

    @Inject XenditService xenditService;

    @Inject
    @RestClient
    AuthClient AuthClient;

    @Inject
    @Channel("offering-accepted")
    Emitter<OfferingAcceptedEvent> offeringAcceptedEmitter;

    @ConfigProperty(name = "tithe.escrow.percentage", defaultValue = "1.0")
    double tithePercentage;

    @ConfigProperty(name = "tithe.withdrawal.flat", defaultValue = "6500")
    BigDecimal withdrawalTitheFlat;

    // ─── 1. GET OR CREATE TREASURY ────────────────────────────────────────────

    @WithTransaction
    public Uni<TreasuryResponse> getOrCreateTreasury(UUID pilgrimId) {
        return treasuryRepo.findByPilgrimId(pilgrimId)
                .flatMap(opt -> {
                    if (opt.isPresent()) {
                        return Uni.createFrom().item(opt.get());
                    }
                    LOG.infof("[VAULT] Forging new Treasury for Pilgrim: %s", pilgrimId);
                    Treasury treasury = Treasury.forPilgrim(pilgrimId);
                    return treasuryRepo.persist(treasury);
                })
                .map(TreasuryResponse::from);
    }

    // ─── 2. MAKE OFFERING (XENDIT TOP-UP INITIATION) ──────────────────────────

    @WithTransaction
    public Uni<OfferingResponse> makeOffering(UUID pilgrimId, MakeOfferingRequest req) {
        // Idempotency check via covenantKey
        Uni<Optional<Offering>> existingCheck = req.covenantKey != null && !req.covenantKey.isBlank()
                ? offeringRepo.findByCovenantKey(req.covenantKey)
                : Uni.createFrom().item(Optional.empty());

        return existingCheck.flatMap(existingOpt -> {
            if (existingOpt.isPresent()) {
                return Uni.createFrom().item(OfferingResponse.from(existingOpt.get()));
            }

            return treasuryRepo.findByPilgrimId(pilgrimId)
                    .flatMap(treasuryOpt -> {
                        Treasury treasury = treasuryOpt.orElseGet(() -> {
                            Treasury t = Treasury.forPilgrim(pilgrimId);
                            treasuryRepo.persist(t);
                            return t;
                        });

                        Offering offering = new Offering();
                        offering.treasuryId = treasury.id;
                        offering.amount = req.amount;
                        offering.tithe = BigDecimal.ZERO; // No tithe on top-ups
                        offering.gateway = "XENDIT";
                        offering.paymentMethod = req.method;
                        offering.status = Offering.OfferingStatus.PENDING;
                        offering.expiresAt = Instant.now().plusSeconds(86400); // 24 hours
                        offering.covenantKey = req.covenantKey;

                        return offeringRepo.persist(offering)
                                .flatMap(savedOffering ->
                                        xenditService.createInvoice(savedOffering.id, req.amount, null, req.method)
                                                .map(json -> {
                                                    savedOffering.gatewayTransactionId = json.getString("id");
                                                    Map<String, Object> details = new HashMap<>();
                                                    details.put("invoice_url", json.getString("invoice_url"));
                                                    details.put("expiry_date", json.getString("expiry_date"));
                                                    savedOffering.paymentDetails = details;
                                                    return savedOffering;
                                                })
                                )
                                .flatMap(offeringRepo::persist)
                                .map(OfferingResponse::from);
                    });
        });
    }

    // ─── 3. HANDLE XENDIT WEBHOOK CALLBACK ────────────────────────────────────

    @WithTransaction
    public Uni<Map<String, Object>> handleXenditWebhook(String headerToken, XenditWebhookPayload payload) {
        if (!xenditService.verifyWebhookToken(headerToken)) {
            throw VaultException.unauthorized("INVALID_WEBHOOK_TOKEN", "Unauthorized callback token");
        }

        String externalId = payload.externalId;
        if (externalId == null || !externalId.startsWith("OFFERING-")) {
            LOG.warnf("[XENDIT WEBHOOK] Ignored non-offering webhook: external_id=%s", externalId);
            return Uni.createFrom().item(Map.of("status", "ignored"));
        }

        UUID offeringId = UUID.fromString(externalId.replace("OFFERING-", ""));

        return offeringRepo.findByIdSafe(offeringId)
                .flatMap(offeringOpt -> {
                    if (offeringOpt.isEmpty()) {
                        LOG.errorf("[XENDIT WEBHOOK] Offering not found: %s", offeringId);
                        return Uni.createFrom().item(Map.of("status", "not_found"));
                    }

                    Offering offering = offeringOpt.get();
                    if (offering.status == Offering.OfferingStatus.ACCEPTED) {
                        LOG.infof("[XENDIT WEBHOOK] Offering already accepted: %s", offeringId);
                        return Uni.createFrom().item(Map.of("status", "already_processed"));
                    }

                    if ("PAID".equalsIgnoreCase(payload.status) || "SETTLED".equalsIgnoreCase(payload.status)) {
                        offering.status = Offering.OfferingStatus.ACCEPTED;
                        offering.acceptedAt = Instant.now();

                        return treasuryRepo.findByIdSafe(offering.treasuryId)
                                .flatMap(treasuryOpt -> {
                                    Treasury treasury = treasuryOpt.orElseThrow(() ->
                                            VaultException.notFound("TREASURY_NOT_FOUND", "Treasury missing"));

                                    BigDecimal before = treasury.availableTreasury;
                                    treasury.availableTreasury = treasury.availableTreasury.add(offering.amount);
                                    treasury.totalOffered = treasury.totalOffered.add(offering.amount);
                                    BigDecimal after = treasury.availableTreasury;

                                    Chronicle chronicle = Chronicle.record(
                                            treasury.id, "OFFERING", offering.amount,
                                            BigDecimal.ZERO, before, after
                                    );
                                    chronicle.referenceId = offering.id.toString();
                                    chronicle.referenceType = "OFFERING";
                                    chronicle.description = "Sacred Offering top-up via Xendit";

                                    return treasuryRepo.persist(treasury)
                                            .flatMap(t -> offeringRepo.persist(offering))
                                            .flatMap(o -> chronicleRepo.persist(chronicle))
                                            .map(c -> {
                                                offeringAcceptedEmitter.send(new OfferingAcceptedEvent(
                                                        offering.id.toString(), treasury.pilgrimId.toString(), offering.amount
                                                ));
                                                return Map.of("status", "accepted", "offeringId", offering.id.toString());
                                            });
                                });
                    } else if ("EXPIRED".equalsIgnoreCase(payload.status)) {
                        offering.status = Offering.OfferingStatus.EXPIRED;
                        return offeringRepo.persist(offering)
                                .map(o -> Map.of("status", "expired"));
                    }

                    return Uni.createFrom().item(Map.of("status", "pending"));
                });
    }

    // ─── 4. REQUEST BANK WITHDRAWAL (DISBURSEMENT) ────────────────────────────

    @WithTransaction
    public Uni<WithdrawalResponse> requestWithdrawal(UUID pilgrimId, WithdrawalRequest req) {
        return treasuryRepo.findByPilgrimId(pilgrimId)
                .flatMap(treasuryOpt -> {
                    Treasury treasury = treasuryOpt.orElseThrow(() ->
                            VaultException.notFound("TREASURY_NOT_FOUND", "Treasury not initialized"));

                    if (!treasury.canDebit(req.amount)) {
                        throw VaultException.insufficientFunds("Insufficient treasury balance for withdrawal");
                    }

                    BigDecimal tithe = withdrawalTitheFlat;
                    BigDecimal netAmount = req.amount.subtract(tithe);

                    if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw VaultException.badRequest("AMOUNT_TOO_LOW", "Amount after tithe fee must be greater than zero");
                    }

                    String maskedAccount = req.accountNumber.length() > 4
                            ? "****" + req.accountNumber.substring(req.accountNumber.length() - 4)
                            : "****";

                    Withdrawal w = new Withdrawal();
                    w.treasuryId = treasury.id;
                    w.amount = req.amount;
                    w.tithe = tithe;
                    w.netAmount = netAmount;
                    w.bankCode = req.bankCode.toUpperCase();
                    w.bankName = req.bankCode.toUpperCase();
                    w.accountNumberHash = BcryptUtil.bcryptHash(req.accountNumber, 10);
                    w.accountNumberMasked = maskedAccount;
                    w.accountName = req.accountName;
                    w.status = Withdrawal.WithdrawalStatus.PROCESSING;
                    w.processedAt = Instant.now();
                    w.covenantKey = req.covenantKey;

                    BigDecimal before = treasury.availableTreasury;
                    treasury.availableTreasury = treasury.availableTreasury.subtract(req.amount);
                    treasury.totalWithdrawn = treasury.totalWithdrawn.add(req.amount);
                    BigDecimal after = treasury.availableTreasury;

                    Chronicle chronicle = Chronicle.record(
                            treasury.id, "WITHDRAWAL", req.amount,
                            tithe, before, after
                    );
                    chronicle.referenceType = "WITHDRAWAL";
                    chronicle.description = "Bank withdrawal to " + req.bankCode + " " + maskedAccount;

                    return treasuryRepo.persist(treasury)
                            .flatMap(t -> withdrawalRepo.persist(w))
                            .flatMap(savedW ->
                                    xenditService.createDisbursement(savedW.id, req.bankCode, req.accountNumber, req.accountName, netAmount)
                                            .map(json -> {
                                                savedW.gatewayTransactionId = json.getString("id");
                                                savedW.status = Withdrawal.WithdrawalStatus.COMPLETED;
                                                savedW.completedAt = Instant.now();
                                                return savedW;
                                            })
                                            .onFailure().recoverWithUni(err -> {
                                                LOG.errorf(err, "[VAULT] Disbursement call failed for %s", savedW.id);
                                                savedW.status = Withdrawal.WithdrawalStatus.FAILED;
                                                // Refund available treasury on failed disbursement
                                                treasury.availableTreasury = treasury.availableTreasury.add(req.amount);
                                                return treasuryRepo.persist(treasury)
                                                        .replaceWith(savedW);
                                            })
                            )
                            .flatMap(withdrawalRepo::persist)
                            .flatMap(finalW -> {
                                chronicle.referenceId = finalW.id.toString();
                                return chronicleRepo.persist(chronicle).replaceWith(finalW);
                            })
                            .map(WithdrawalResponse::from);
                });
    }

    // ─── 5. P2P TITHING TRANSFER ──────────────────────────────────────────────

    @WithTransaction
    public Uni<ChronicleResponse> tithingTransfer(UUID senderPilgrimId, UUID recipientPilgrimId, TitheTransferRequest req) {
        if (senderPilgrimId.equals(recipientPilgrimId)) {
            throw VaultException.badRequest("INVALID_TRANSFER", "Cannot transfer tithe to oneself");
        }

        return treasuryRepo.findByPilgrimId(senderPilgrimId)
                .flatMap(senderOpt -> {
                    Treasury senderTreasury = senderOpt.orElseThrow(() ->
                            VaultException.notFound("TREASURY_NOT_FOUND", "Sender treasury not found"));

                    if (!senderTreasury.canDebit(req.amount)) {
                        throw VaultException.insufficientFunds("Insufficient balance for transfer");
                    }

                    return treasuryRepo.findByPilgrimId(recipientPilgrimId)
                            .flatMap(recipientOpt -> {
                                Treasury recipientTreasury = recipientOpt.orElseGet(() -> {
                                    Treasury t = Treasury.forPilgrim(recipientPilgrimId);
                                    treasuryRepo.persist(t);
                                    return t;
                                });

                                BigDecimal senderBefore = senderTreasury.availableTreasury;
                                senderTreasury.availableTreasury = senderTreasury.availableTreasury.subtract(req.amount);
                                BigDecimal senderAfter = senderTreasury.availableTreasury;

                                BigDecimal recipientBefore = recipientTreasury.availableTreasury;
                                recipientTreasury.availableTreasury = recipientTreasury.availableTreasury.add(req.amount);
                                BigDecimal recipientAfter = recipientTreasury.availableTreasury;

                                Chronicle senderChronicle = Chronicle.record(
                                        senderTreasury.id, "TITHING_OUT", req.amount,
                                        BigDecimal.ZERO, senderBefore, senderAfter
                                );
                                senderChronicle.counterpartyTreasuryId = recipientTreasury.id;
                                senderChronicle.description = req.description != null ? req.description : "P2P Tithe Transfer Out";

                                Chronicle recipientChronicle = Chronicle.record(
                                        recipientTreasury.id, "TITHING_IN", req.amount,
                                        BigDecimal.ZERO, recipientBefore, recipientAfter
                                );
                                recipientChronicle.counterpartyTreasuryId = senderTreasury.id;
                                recipientChronicle.description = req.description != null ? req.description : "P2P Tithe Transfer In";

                                return treasuryRepo.persist(senderTreasury)
                                        .flatMap(t -> treasuryRepo.persist(recipientTreasury))
                                        .flatMap(t -> chronicleRepo.persist(senderChronicle))
                                        .flatMap(sc -> chronicleRepo.persist(recipientChronicle))
                                        .map(ChronicleResponse::from);
                            });
                });
    }

    // ─── 6. ESCROW: SEAL HOLD (LOCK FUNDS FOR COVENANT) ──────────────────────

    @WithTransaction
    public Uni<Void> sealEscrowHold(UUID buyerId, BigDecimal amount, String covenantId) {
        return treasuryRepo.findByPilgrimId(buyerId)
                .flatMap(opt -> {
                    Treasury treasury = opt.orElseThrow(() ->
                            VaultException.notFound("TREASURY_NOT_FOUND", "Buyer treasury not found"));

                    if (!treasury.canDebit(amount)) {
                        throw VaultException.insufficientFunds("Insufficient balance to lock in escrow");
                    }

                    BigDecimal before = treasury.availableTreasury;
                    treasury.availableTreasury = treasury.availableTreasury.subtract(amount);
                    treasury.sealedTreasury = treasury.sealedTreasury.add(amount);
                    BigDecimal after = treasury.availableTreasury;

                    Chronicle chronicle = Chronicle.record(
                            treasury.id, "SEAL_HOLD", amount,
                            BigDecimal.ZERO, before, after
                    );
                    chronicle.referenceId = covenantId;
                    chronicle.referenceType = "COVENANT";
                    chronicle.description = "Funds held in Escrow for Covenant: " + covenantId;

                    return treasuryRepo.persist(treasury)
                            .flatMap(t -> chronicleRepo.persist(chronicle))
                            .replaceWithVoid();
                });
    }

    // ─── 7. ESCROW: RELEASE (PAY SELLER + DEDUCT TITHE) ───────────────────────

    @WithTransaction
    public Uni<Void> releaseEscrow(UUID buyerId, UUID sellerId, BigDecimal amount, String covenantId) {
        return treasuryRepo.findByPilgrimId(buyerId)
                .flatMap(buyerOpt -> {
                    Treasury buyerTreasury = buyerOpt.orElseThrow(() ->
                            VaultException.notFound("TREASURY_NOT_FOUND", "Buyer treasury not found"));

                    return treasuryRepo.findByPilgrimId(sellerId)
                            .flatMap(sellerOpt -> {
                                Treasury sellerTreasury = sellerOpt.orElseGet(() -> {
                                    Treasury t = Treasury.forPilgrim(sellerId);
                                    treasuryRepo.persist(t);
                                    return t;
                                });

                                BigDecimal titheAmount = amount.multiply(BigDecimal.valueOf(tithePercentage / 100.0));
                                BigDecimal payoutAmount = amount.subtract(titheAmount);

                                // Deduct from buyer's sealed treasury
                                BigDecimal buyerBefore = buyerTreasury.sealedTreasury;
                                buyerTreasury.sealedTreasury = buyerTreasury.sealedTreasury.subtract(amount);

                                // Credit seller's available treasury
                                BigDecimal sellerBefore = sellerTreasury.availableTreasury;
                                sellerTreasury.availableTreasury = sellerTreasury.availableTreasury.add(payoutAmount);
                                BigDecimal sellerAfter = sellerTreasury.availableTreasury;

                                Chronicle buyerChronicle = Chronicle.record(
                                        buyerTreasury.id, "SEAL_RELEASE", amount,
                                        titheAmount, buyerBefore, buyerTreasury.sealedTreasury
                                );
                                buyerChronicle.referenceId = covenantId;
                                buyerChronicle.referenceType = "COVENANT";
                                buyerChronicle.description = "Escrow released to seller for Covenant: " + covenantId;

                                Chronicle sellerChronicle = Chronicle.record(
                                        sellerTreasury.id, "SEAL_RELEASE", payoutAmount,
                                        titheAmount, sellerBefore, sellerAfter
                                );
                                sellerChronicle.referenceId = covenantId;
                                sellerChronicle.referenceType = "COVENANT";
                                sellerChronicle.description = "Escrow payout received for Covenant: " + covenantId;

                                return treasuryRepo.persist(buyerTreasury)
                                        .flatMap(t -> treasuryRepo.persist(sellerTreasury))
                                        .flatMap(t -> chronicleRepo.persist(buyerChronicle))
                                        .flatMap(c -> chronicleRepo.persist(sellerChronicle))
                                        .replaceWithVoid();
                            });
                });
    }

    // ─── 8. ESCROW: REFUND (RETURN FUNDS TO BUYER) ────────────────────────────

    @WithTransaction
    public Uni<Void> refundEscrow(UUID buyerId, BigDecimal amount, String covenantId) {
        return treasuryRepo.findByPilgrimId(buyerId)
                .flatMap(opt -> {
                    Treasury treasury = opt.orElseThrow(() ->
                            VaultException.notFound("TREASURY_NOT_FOUND", "Buyer treasury not found"));

                    BigDecimal before = treasury.availableTreasury;
                    treasury.sealedTreasury = treasury.sealedTreasury.subtract(amount);
                    treasury.availableTreasury = treasury.availableTreasury.add(amount);
                    BigDecimal after = treasury.availableTreasury;

                    Chronicle chronicle = Chronicle.record(
                            treasury.id, "SEAL_REFUND", amount,
                            BigDecimal.ZERO, before, after
                    );
                    chronicle.referenceId = covenantId;
                    chronicle.referenceType = "COVENANT";
                    chronicle.description = "Escrow refunded for Covenant: " + covenantId;

                    return treasuryRepo.persist(treasury)
                            .flatMap(t -> chronicleRepo.persist(chronicle))
                            .replaceWithVoid();
                });
    }

    // ─── 9. GET CHRONICLES (AUDIT HISTORY) ───────────────────────────────────

    public Uni<List<ChronicleResponse>> getChronicles(UUID pilgrimId, int page, int size) {
        return treasuryRepo.findByPilgrimId(pilgrimId)
                .flatMap(opt -> {
                    if (opt.isEmpty()) return Uni.createFrom().item(Collections.emptyList());
                    return chronicleRepo.findByTreasuryId(opt.get().id, page, size)
                            .map(list -> list.stream().map(ChronicleResponse::from).toList());
                });
    }
}
