package com.eyesofpriestess.wallet.service;

import com.eyesofpriestess.wallet.client.AuthClient;
import com.eyesofpriestess.wallet.dto.request.*;
import com.eyesofpriestess.wallet.dto.response.*;
import com.eyesofpriestess.wallet.entity.*;
import com.eyesofpriestess.wallet.event.TopUpPaidEvent;
import com.eyesofpriestess.wallet.exception.WalletException;
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
 * WalletService — Core business engine for Vault Sanctum (Wallet & Payments).
 *
 * Implements:
 * - Wallet lifecycle (Auto-creation on first access)
 * - Xendit Top-Up (TopUpOrder creation & webhook processing)
 * - Xendit Bank Withdrawal (Disbursement)
 * - P2P Tithing Transfer (Balance transfer between users)
 * - Escrow Management (Seal Hold, Release, Refund for Room Sanctum)
 * - Transaction audit logging
 */
@ApplicationScoped
public class WalletService {

    private static final Logger LOG = Logger.getLogger(WalletService.class);

    @Inject WalletRepository treasuryRepo;
    @Inject TopUpOrderRepository offeringRepo;
    @Inject WithdrawalRepository withdrawalRepo;
    @Inject TransactionRepository chronicleRepo;

    @Inject XenditService xenditService;

    @Inject
    @RestClient
    AuthClient AuthClient;

    @Inject
    @Channel("TopUpOrder-accepted")
    Emitter<TopUpPaidEvent> offeringAcceptedEmitter;

    @ConfigProperty(name = "tithe.escrow.percentage", defaultValue = "1.0")
    double tithePercentage;

    @ConfigProperty(name = "tithe.withdrawal.flat", defaultValue = "6500")
    BigDecimal withdrawalTitheFlat;

    // ─── 1. GET OR CREATE Wallet ────────────────────────────────────────────

    @WithTransaction
    public Uni<WalletResponse> getOrCreateTreasury(UUID userId) {
        return treasuryRepo.findByPilgrimId(userId)
                .flatMap(opt -> {
                    if (opt.isPresent()) {
                        return Uni.createFrom().item(opt.get());
                    }
                    LOG.infof("[VAULT] Forging new Wallet for User: %s", userId);
                    Wallet wallet = Wallet.forPilgrim(userId);
                    return treasuryRepo.persist(wallet);
                })
                .map(WalletResponse::from);
    }

    // ─── 2. MAKE TopUpOrder (XENDIT TOP-UP INITIATION) ──────────────────────────

    @WithTransaction
    public Uni<TopUpResponse> makeOffering(UUID userId, CreateTopUpRequest req) {
        // Idempotency check via covenantKey
        Uni<Optional<TopUpOrder>> existingCheck = req.covenantKey != null && !req.covenantKey.isBlank()
                ? offeringRepo.findByCovenantKey(req.covenantKey)
                : Uni.createFrom().item(Optional.empty());

        return existingCheck.flatMap(existingOpt -> {
            if (existingOpt.isPresent()) {
                return Uni.createFrom().item(TopUpResponse.from(existingOpt.get()));
            }

            return treasuryRepo.findByPilgrimId(userId)
                    .flatMap(treasuryOpt -> {
                        Wallet wallet = treasuryOpt.orElseGet(() -> {
                            Wallet t = Wallet.forPilgrim(userId);
                            treasuryRepo.persist(t);
                            return t;
                        });

                        TopUpOrder topUpOrder = new TopUpOrder();
                        topUpOrder.treasuryId = wallet.id;
                        topUpOrder.amount = req.amount;
                        topUpOrder.tithe = BigDecimal.ZERO; // No tithe on top-ups
                        topUpOrder.gateway = "XENDIT";
                        topUpOrder.paymentMethod = req.method;
                        topUpOrder.status = TopUpOrder.OfferingStatus.PENDING;
                        topUpOrder.expiresAt = Instant.now().plusSeconds(86400); // 24 hours
                        topUpOrder.covenantKey = req.covenantKey;

                        return offeringRepo.persist(topUpOrder)
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
                                .map(TopUpResponse::from);
                    });
        });
    }

    // ─── 3. HANDLE XENDIT WEBHOOK CALLBACK ────────────────────────────────────

    @WithTransaction
    public Uni<Map<String, Object>> handleXenditWebhook(String headerToken, XenditWebhookPayload payload) {
        if (!xenditService.verifyWebhookToken(headerToken)) {
            throw WalletException.unauthorized("INVALID_WEBHOOK_TOKEN", "Unauthorized callback token");
        }

        String externalId = payload.externalId;
        if (externalId == null || !externalId.startsWith("TopUpOrder-")) {
            LOG.warnf("[XENDIT WEBHOOK] Ignored non-TopUpOrder webhook: external_id=%s", externalId);
            return Uni.createFrom().item(Map.of("status", "ignored"));
        }

        UUID offeringId = UUID.fromString(externalId.replace("TopUpOrder-", ""));

        return offeringRepo.findByIdSafe(offeringId)
                .flatMap(offeringOpt -> {
                    if (offeringOpt.isEmpty()) {
                        LOG.errorf("[XENDIT WEBHOOK] TopUpOrder not found: %s", offeringId);
                        return Uni.createFrom().item(Map.of("status", "not_found"));
                    }

                    TopUpOrder topUpOrder = offeringOpt.get();
                    if (topUpOrder.status == TopUpOrder.OfferingStatus.ACCEPTED) {
                        LOG.infof("[XENDIT WEBHOOK] TopUpOrder already accepted: %s", offeringId);
                        return Uni.createFrom().item(Map.of("status", "already_processed"));
                    }

                    if ("PAID".equalsIgnoreCase(payload.status) || "SETTLED".equalsIgnoreCase(payload.status)) {
                        topUpOrder.status = TopUpOrder.OfferingStatus.ACCEPTED;
                        topUpOrder.acceptedAt = Instant.now();

                        return treasuryRepo.findByIdSafe(topUpOrder.treasuryId)
                                .flatMap(treasuryOpt -> {
                                    Wallet wallet = treasuryOpt.orElseThrow(() ->
                                            WalletException.notFound("TREASURY_NOT_FOUND", "Wallet missing"));

                                    BigDecimal before = wallet.availableTreasury;
                                    wallet.availableTreasury = wallet.availableTreasury.add(topUpOrder.amount);
                                    wallet.totalOffered = wallet.totalOffered.add(topUpOrder.amount);
                                    BigDecimal after = wallet.availableTreasury;

                                    Transaction transaction = Transaction.record(
                                            wallet.id, "TopUpOrder", topUpOrder.amount,
                                            BigDecimal.ZERO, before, after
                                    );
                                    transaction.referenceId = topUpOrder.id.toString();
                                    transaction.referenceType = "TopUpOrder";
                                    transaction.description = "Sacred TopUpOrder top-up via Xendit";

                                    return treasuryRepo.persist(wallet)
                                            .flatMap(t -> offeringRepo.persist(topUpOrder))
                                            .flatMap(o -> chronicleRepo.persist(transaction))
                                            .map(c -> {
                                                offeringAcceptedEmitter.send(new TopUpPaidEvent(
                                                        topUpOrder.id.toString(), wallet.userId.toString(), topUpOrder.amount
                                                ));
                                                return Map.of("status", "accepted", "offeringId", topUpOrder.id.toString());
                                            });
                                });
                    } else if ("EXPIRED".equalsIgnoreCase(payload.status)) {
                        topUpOrder.status = TopUpOrder.OfferingStatus.EXPIRED;
                        return offeringRepo.persist(topUpOrder)
                                .map(o -> Map.of("status", "expired"));
                    }

                    return Uni.createFrom().item(Map.of("status", "pending"));
                });
    }

    // ─── 4. REQUEST BANK WITHDRAWAL (DISBURSEMENT) ────────────────────────────

    @WithTransaction
    public Uni<WithdrawalResponse> requestWithdrawal(UUID userId, WithdrawalRequest req) {
        return treasuryRepo.findByPilgrimId(userId)
                .flatMap(treasuryOpt -> {
                    Wallet wallet = treasuryOpt.orElseThrow(() ->
                            WalletException.notFound("TREASURY_NOT_FOUND", "Wallet not initialized"));

                    if (!wallet.canDebit(req.amount)) {
                        throw WalletException.insufficientFunds("Insufficient Wallet balance for withdrawal");
                    }

                    BigDecimal tithe = withdrawalTitheFlat;
                    BigDecimal netAmount = req.amount.subtract(tithe);

                    if (netAmount.compareTo(BigDecimal.ZERO) <= 0) {
                        throw WalletException.badRequest("AMOUNT_TOO_LOW", "Amount after tithe fee must be greater than zero");
                    }

                    String maskedAccount = req.accountNumber.length() > 4
                            ? "****" + req.accountNumber.substring(req.accountNumber.length() - 4)
                            : "****";

                    Withdrawal w = new Withdrawal();
                    w.treasuryId = wallet.id;
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

                    BigDecimal before = wallet.availableTreasury;
                    wallet.availableTreasury = wallet.availableTreasury.subtract(req.amount);
                    wallet.totalWithdrawn = wallet.totalWithdrawn.add(req.amount);
                    BigDecimal after = wallet.availableTreasury;

                    Transaction transaction = Transaction.record(
                            wallet.id, "WITHDRAWAL", req.amount,
                            tithe, before, after
                    );
                    transaction.referenceType = "WITHDRAWAL";
                    transaction.description = "Bank withdrawal to " + req.bankCode + " " + maskedAccount;

                    return treasuryRepo.persist(wallet)
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
                                                // Refund available Wallet on failed disbursement
                                                wallet.availableTreasury = wallet.availableTreasury.add(req.amount);
                                                return treasuryRepo.persist(wallet)
                                                        .replaceWith(savedW);
                                            })
                            )
                            .flatMap(withdrawalRepo::persist)
                            .flatMap(finalW -> {
                                transaction.referenceId = finalW.id.toString();
                                return chronicleRepo.persist(transaction).replaceWith(finalW);
                            })
                            .map(WithdrawalResponse::from);
                });
    }

    // ─── 5. P2P TITHING TRANSFER ──────────────────────────────────────────────

    @WithTransaction
    public Uni<TransactionResponse> tithingTransfer(UUID senderPilgrimId, UUID recipientPilgrimId, TransferRequest req) {
        if (senderPilgrimId.equals(recipientPilgrimId)) {
            throw WalletException.badRequest("INVALID_TRANSFER", "Cannot transfer tithe to oneself");
        }

        return treasuryRepo.findByPilgrimId(senderPilgrimId)
                .flatMap(senderOpt -> {
                    Wallet senderTreasury = senderOpt.orElseThrow(() ->
                            WalletException.notFound("TREASURY_NOT_FOUND", "Sender Wallet not found"));

                    if (!senderTreasury.canDebit(req.amount)) {
                        throw WalletException.insufficientFunds("Insufficient balance for transfer");
                    }

                    return treasuryRepo.findByPilgrimId(recipientPilgrimId)
                            .flatMap(recipientOpt -> {
                                Wallet recipientTreasury = recipientOpt.orElseGet(() -> {
                                    Wallet t = Wallet.forPilgrim(recipientPilgrimId);
                                    treasuryRepo.persist(t);
                                    return t;
                                });

                                BigDecimal senderBefore = senderTreasury.availableTreasury;
                                senderTreasury.availableTreasury = senderTreasury.availableTreasury.subtract(req.amount);
                                BigDecimal senderAfter = senderTreasury.availableTreasury;

                                BigDecimal recipientBefore = recipientTreasury.availableTreasury;
                                recipientTreasury.availableTreasury = recipientTreasury.availableTreasury.add(req.amount);
                                BigDecimal recipientAfter = recipientTreasury.availableTreasury;

                                Transaction senderChronicle = Transaction.record(
                                        senderTreasury.id, "TITHING_OUT", req.amount,
                                        BigDecimal.ZERO, senderBefore, senderAfter
                                );
                                senderChronicle.counterpartyTreasuryId = recipientTreasury.id;
                                senderChronicle.description = req.description != null ? req.description : "P2P Tithe Transfer Out";

                                Transaction recipientChronicle = Transaction.record(
                                        recipientTreasury.id, "TITHING_IN", req.amount,
                                        BigDecimal.ZERO, recipientBefore, recipientAfter
                                );
                                recipientChronicle.counterpartyTreasuryId = senderTreasury.id;
                                recipientChronicle.description = req.description != null ? req.description : "P2P Tithe Transfer In";

                                return treasuryRepo.persist(senderTreasury)
                                        .flatMap(t -> treasuryRepo.persist(recipientTreasury))
                                        .flatMap(t -> chronicleRepo.persist(senderChronicle))
                                        .flatMap(sc -> chronicleRepo.persist(recipientChronicle))
                                        .map(TransactionResponse::from);
                            });
                });
    }

    // ─── 6. ESCROW: SEAL HOLD (LOCK FUNDS FOR Room) ──────────────────────

    @WithTransaction
    public Uni<Void> sealEscrowHold(UUID buyerId, BigDecimal amount, String roomId) {
        return treasuryRepo.findByPilgrimId(buyerId)
                .flatMap(opt -> {
                    Wallet wallet = opt.orElseThrow(() ->
                            WalletException.notFound("TREASURY_NOT_FOUND", "Buyer Wallet not found"));

                    if (!wallet.canDebit(amount)) {
                        throw WalletException.insufficientFunds("Insufficient balance to lock in escrow");
                    }

                    BigDecimal before = wallet.availableTreasury;
                    wallet.availableTreasury = wallet.availableTreasury.subtract(amount);
                    wallet.sealedTreasury = wallet.sealedTreasury.add(amount);
                    BigDecimal after = wallet.availableTreasury;

                    Transaction transaction = Transaction.record(
                            wallet.id, "SEAL_HOLD", amount,
                            BigDecimal.ZERO, before, after
                    );
                    transaction.referenceId = roomId;
                    transaction.referenceType = "Room";
                    transaction.description = "Funds held in Escrow for Room: " + roomId;

                    return treasuryRepo.persist(wallet)
                            .flatMap(t -> chronicleRepo.persist(transaction))
                            .replaceWithVoid();
                });
    }

    // ─── 7. ESCROW: RELEASE (PAY SELLER + DEDUCT TITHE) ───────────────────────

    @WithTransaction
    public Uni<Void> releaseEscrow(UUID buyerId, UUID sellerId, BigDecimal amount, String roomId) {
        return treasuryRepo.findByPilgrimId(buyerId)
                .flatMap(buyerOpt -> {
                    Wallet buyerTreasury = buyerOpt.orElseThrow(() ->
                            WalletException.notFound("TREASURY_NOT_FOUND", "Buyer Wallet not found"));

                    return treasuryRepo.findByPilgrimId(sellerId)
                            .flatMap(sellerOpt -> {
                                Wallet sellerTreasury = sellerOpt.orElseGet(() -> {
                                    Wallet t = Wallet.forPilgrim(sellerId);
                                    treasuryRepo.persist(t);
                                    return t;
                                });

                                BigDecimal titheAmount = amount.multiply(BigDecimal.valueOf(tithePercentage / 100.0));
                                BigDecimal payoutAmount = amount.subtract(titheAmount);

                                // Deduct from buyer's sealed Wallet
                                BigDecimal buyerBefore = buyerTreasury.sealedTreasury;
                                buyerTreasury.sealedTreasury = buyerTreasury.sealedTreasury.subtract(amount);

                                // Credit seller's available Wallet
                                BigDecimal sellerBefore = sellerTreasury.availableTreasury;
                                sellerTreasury.availableTreasury = sellerTreasury.availableTreasury.add(payoutAmount);
                                BigDecimal sellerAfter = sellerTreasury.availableTreasury;

                                Transaction buyerChronicle = Transaction.record(
                                        buyerTreasury.id, "SEAL_RELEASE", amount,
                                        titheAmount, buyerBefore, buyerTreasury.sealedTreasury
                                );
                                buyerChronicle.referenceId = roomId;
                                buyerChronicle.referenceType = "Room";
                                buyerChronicle.description = "Escrow released to seller for Room: " + roomId;

                                Transaction sellerChronicle = Transaction.record(
                                        sellerTreasury.id, "SEAL_RELEASE", payoutAmount,
                                        titheAmount, sellerBefore, sellerAfter
                                );
                                sellerChronicle.referenceId = roomId;
                                sellerChronicle.referenceType = "Room";
                                sellerChronicle.description = "Escrow payout received for Room: " + roomId;

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
    public Uni<Void> refundEscrow(UUID buyerId, BigDecimal amount, String roomId) {
        return treasuryRepo.findByPilgrimId(buyerId)
                .flatMap(opt -> {
                    Wallet wallet = opt.orElseThrow(() ->
                            WalletException.notFound("TREASURY_NOT_FOUND", "Buyer Wallet not found"));

                    BigDecimal before = wallet.availableTreasury;
                    wallet.sealedTreasury = wallet.sealedTreasury.subtract(amount);
                    wallet.availableTreasury = wallet.availableTreasury.add(amount);
                    BigDecimal after = wallet.availableTreasury;

                    Transaction transaction = Transaction.record(
                            wallet.id, "SEAL_REFUND", amount,
                            BigDecimal.ZERO, before, after
                    );
                    transaction.referenceId = roomId;
                    transaction.referenceType = "Room";
                    transaction.description = "Escrow refunded for Room: " + roomId;

                    return treasuryRepo.persist(wallet)
                            .flatMap(t -> chronicleRepo.persist(transaction))
                            .replaceWithVoid();
                });
    }

    // ─── 9. GET transactions (AUDIT HISTORY) ───────────────────────────────────

    public Uni<List<TransactionResponse>> getChronicles(UUID userId, int page, int size) {
        return treasuryRepo.findByPilgrimId(userId)
                .flatMap(opt -> {
                    if (opt.isEmpty()) return Uni.createFrom().item(Collections.emptyList());
                    return chronicleRepo.findByTreasuryId(opt.get().id, page, size)
                            .map(list -> list.stream().map(TransactionResponse::from).toList());
                });
    }
}
