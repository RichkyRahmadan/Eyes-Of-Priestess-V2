package com.eyesofpriestess.covenant.service;

import com.eyesofpriestess.covenant.client.VaultClient;
import com.eyesofpriestess.covenant.dto.request.*;
import com.eyesofpriestess.covenant.dto.response.*;
import com.eyesofpriestess.covenant.entity.*;
import com.eyesofpriestess.covenant.event.CovenantBrokenEvent;
import com.eyesofpriestess.covenant.event.CovenantFulfilledEvent;
import com.eyesofpriestess.covenant.exception.CovenantException;
import com.eyesofpriestess.covenant.repository.*;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

/**
 * CovenantService — Core Escrow Room state machine engine.
 *
 * Manages:
 * - Covenant creation & invitation code generation
 * - Acceptance by counterparty & role binding (Buyer/Seller)
 * - Escrow deposit & hold via Vault Sanctum
 * - Delivery declaration by Seller
 * - Fulfillment confirmation & payout release
 * - Dispute initiation & Cancellation
 */
@ApplicationScoped
public class CovenantService {

    private static final Logger LOG = Logger.getLogger(CovenantService.class);
    private static final String ALPHANUM = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Inject CovenantRepository covenantRepo;
    @Inject InvitationRepository invitationRepo;

    @Inject
    @RestClient
    VaultClient vaultClient;

    @Inject
    @Channel("covenant-fulfilled")
    Emitter<CovenantFulfilledEvent> covenantFulfilledEmitter;

    @Inject
    @Channel("covenant-broken")
    Emitter<CovenantBrokenEvent> covenantBrokenEmitter;

    // ─── 1. FORGE COVENANT (CREATE ESCROW ROOM) ───────────────────────────────

    @WithTransaction
    public Uni<CovenantResponse> forgeCovenant(UUID initiatorId, ForgeCovenantRequest req) {
        Covenant covenant = new Covenant();
        covenant.title = req.title;
        covenant.description = req.description;
        covenant.initiatorId = initiatorId;
        covenant.initiatorRole = req.initiatorRole.toUpperCase();
        covenant.amount = req.amount;
        covenant.status = Covenant.CovenantStatus.FORGED;
        covenant.autoReleaseHours = req.autoReleaseHours != null ? req.autoReleaseHours : 48;
        covenant.expiresAt = Instant.now().plusSeconds(7 * 86400); // 7 days invitation window

        if ("BUYER".equalsIgnoreCase(req.initiatorRole)) {
            covenant.buyerId = initiatorId;
        } else {
            covenant.sellerId = initiatorId;
        }

        return covenantRepo.persist(covenant)
                .flatMap(saved -> {
                    String code = generateInvitationCode();
                    CovenantInvitation inv = new CovenantInvitation();
                    inv.covenantId = saved.id;
                    inv.invitationCode = code;
                    inv.expiresAt = saved.expiresAt;

                    return invitationRepo.persist(inv)
                            .map(i -> {
                                CovenantResponse resp = CovenantResponse.from(saved);
                                resp.invitationCode = i.invitationCode;
                                return resp;
                            });
                });
    }

    // ─── 2. ACCEPT COVENANT (JOIN ESCROW ROOM) ────────────────────────────────

    @WithTransaction
    public Uni<CovenantResponse> acceptCovenant(UUID counterpartyId, String invitationCode) {
        return invitationRepo.findByCode(invitationCode)
                .flatMap(invOpt -> {
                    CovenantInvitation inv = invOpt.orElseThrow(() ->
                            CovenantException.notFound("INVITATION_NOT_FOUND", "Invalid invitation code"));

                    if (inv.isClaimed) {
                        throw CovenantException.conflict("INVITATION_CLAIMED", "Invitation code has already been claimed", "invitationCode");
                    }

                    if (Instant.now().isAfter(inv.expiresAt)) {
                        throw CovenantException.badRequest("INVITATION_EXPIRED", "Invitation code has expired");
                    }

                    return covenantRepo.findByIdSafe(inv.covenantId)
                            .flatMap(covOpt -> {
                                Covenant cov = covOpt.orElseThrow(() ->
                                        CovenantException.notFound("COVENANT_NOT_FOUND", "Covenant room missing"));

                                if (cov.initiatorId.equals(counterpartyId)) {
                                    throw CovenantException.badRequest("INVALID_COUNTERPARTY", "Initiator cannot accept their own covenant");
                                }

                                if (cov.status != Covenant.CovenantStatus.FORGED) {
                                    throw CovenantException.conflict("INVALID_STATE", "Covenant is no longer open for acceptance", "status");
                                }

                                cov.counterpartyId = counterpartyId;
                                cov.status = Covenant.CovenantStatus.ACCEPTED;

                                if ("BUYER".equalsIgnoreCase(cov.initiatorRole)) {
                                    cov.sellerId = counterpartyId;
                                } else {
                                    cov.buyerId = counterpartyId;
                                }

                                inv.isClaimed = true;
                                inv.claimedBy = counterpartyId;

                                return covenantRepo.persist(cov)
                                        .flatMap(c -> invitationRepo.persist(inv).replaceWith(c))
                                        .map(CovenantResponse::from);
                            });
                });
    }

    // ─── 3. SEAL COVENANT (DEPOSIT FUNDS INTO ESCROW) ─────────────────────────

    @WithTransaction
    public Uni<CovenantResponse> sealCovenant(UUID pilgrimId, UUID covenantId) {
        return covenantRepo.findByIdSafe(covenantId)
                .flatMap(covOpt -> {
                    Covenant cov = covOpt.orElseThrow(() ->
                            CovenantException.notFound("COVENANT_NOT_FOUND", "Covenant room not found"));

                    if (!pilgrimId.equals(cov.buyerId)) {
                        throw CovenantException.forbidden("ONLY_BUYER_CAN_SEAL", "Only the designated buyer can deposit funds into escrow");
                    }

                    if (cov.status != Covenant.CovenantStatus.ACCEPTED) {
                        throw CovenantException.conflict("INVALID_STATE", "Covenant must be ACCEPTED before sealing", "status");
                    }

                    // Invoke Vault Sanctum to lock funds
                    return vaultClient.holdEscrow(cov.buyerId, cov.amount, cov.id.toString())
                            .flatMap(res -> {
                                if (!res.success) {
                                    throw CovenantException.badRequest("ESCROW_HOLD_FAILED",
                                            res.error != null ? res.error.message : "Failed to hold funds in Vault");
                                }

                                cov.status = Covenant.CovenantStatus.SEALED;
                                cov.sealedAt = Instant.now();

                                return covenantRepo.persist(cov)
                                        .map(CovenantResponse::from);
                            });
                });
    }

    // ─── 4. DELIVER COVENANT (SELLER MARKS COMPLETED) ─────────────────────────

    @WithTransaction
    public Uni<CovenantResponse> deliverCovenant(UUID pilgrimId, UUID covenantId, DeliverCovenantRequest req) {
        return covenantRepo.findByIdSafe(covenantId)
                .flatMap(covOpt -> {
                    Covenant cov = covOpt.orElseThrow(() ->
                            CovenantException.notFound("COVENANT_NOT_FOUND", "Covenant room not found"));

                    if (!pilgrimId.equals(cov.sellerId)) {
                        throw CovenantException.forbidden("ONLY_SELLER_CAN_DELIVER", "Only the seller can mark delivery");
                    }

                    if (cov.status != Covenant.CovenantStatus.SEALED) {
                        throw CovenantException.conflict("INVALID_STATE", "Covenant must be SEALED before delivery", "status");
                    }

                    cov.status = Covenant.CovenantStatus.DELIVERED;
                    cov.deliveredAt = Instant.now();
                    cov.autoReleaseAt = Instant.now().plusSeconds(cov.autoReleaseHours * 3600L);

                    return covenantRepo.persist(cov)
                            .map(CovenantResponse::from);
                });
    }

    // ─── 5. FULFILL COVENANT (BUYER CONFIRMS / PAYOUT RELEASE) ────────────────

    @WithTransaction
    public Uni<CovenantResponse> fulfillCovenant(UUID pilgrimId, UUID covenantId) {
        return covenantRepo.findByIdSafe(covenantId)
                .flatMap(covOpt -> {
                    Covenant cov = covOpt.orElseThrow(() ->
                            CovenantException.notFound("COVENANT_NOT_FOUND", "Covenant room not found"));

                    if (cov.status != Covenant.CovenantStatus.DELIVERED && cov.status != Covenant.CovenantStatus.SEALED) {
                        throw CovenantException.conflict("INVALID_STATE", "Covenant cannot be fulfilled in status " + cov.status, "status");
                    }

                    if (pilgrimId != null && !pilgrimId.equals(cov.buyerId)) {
                        throw CovenantException.forbidden("ONLY_BUYER_CAN_CONFIRM", "Only the buyer can confirm fulfillment");
                    }

                    // Invoke Vault Sanctum to release payout to seller
                    return vaultClient.releaseEscrow(cov.buyerId, cov.sellerId, cov.amount, cov.id.toString())
                            .flatMap(res -> {
                                cov.status = Covenant.CovenantStatus.FULFILLED;
                                cov.fulfilledAt = Instant.now();

                                return covenantRepo.persist(cov)
                                        .map(c -> {
                                            covenantFulfilledEmitter.send(new CovenantFulfilledEvent(
                                                    cov.id.toString(), cov.buyerId.toString(), cov.sellerId.toString(), cov.amount
                                            ));
                                            return CovenantResponse.from(c);
                                        });
                            });
                });
    }

    // ─── 6. DISPUTE COVENANT (OPEN JUDGMENT CASE) ─────────────────────────────

    @WithTransaction
    public Uni<CovenantResponse> disputeCovenant(UUID pilgrimId, UUID covenantId, DisputeCovenantRequest req) {
        return covenantRepo.findByIdSafe(covenantId)
                .flatMap(covOpt -> {
                    Covenant cov = covOpt.orElseThrow(() ->
                            CovenantException.notFound("COVENANT_NOT_FOUND", "Covenant room not found"));

                    if (!pilgrimId.equals(cov.buyerId) && !pilgrimId.equals(cov.sellerId)) {
                        throw CovenantException.forbidden("NOT_PARTY_TO_COVENANT", "Only covenant parties can initiate a dispute");
                    }

                    if (cov.status != Covenant.CovenantStatus.SEALED && cov.status != Covenant.CovenantStatus.DELIVERED) {
                        throw CovenantException.conflict("INVALID_STATE", "Cannot dispute unsealed or already completed covenant", "status");
                    }

                    cov.status = Covenant.CovenantStatus.DISPUTED;
                    cov.disputedAt = Instant.now();

                    return covenantRepo.persist(cov)
                            .map(c -> {
                                covenantBrokenEmitter.send(new CovenantBrokenEvent(
                                        cov.id.toString(), cov.initiatorId.toString(),
                                        cov.counterpartyId != null ? cov.counterpartyId.toString() : "", req.reason
                                ));
                                return CovenantResponse.from(c);
                            });
                });
    }

    // ─── 7. CANCEL COVENANT ───────────────────────────────────────────────────

    @WithTransaction
    public Uni<CovenantResponse> cancelCovenant(UUID pilgrimId, UUID covenantId) {
        return covenantRepo.findByIdSafe(covenantId)
                .flatMap(covOpt -> {
                    Covenant cov = covOpt.orElseThrow(() ->
                            CovenantException.notFound("COVENANT_NOT_FOUND", "Covenant room not found"));

                    if (!pilgrimId.equals(cov.initiatorId)) {
                        throw CovenantException.forbidden("ONLY_INITIATOR_CAN_CANCEL", "Only the initiator can cancel this covenant");
                    }

                    if (cov.status != Covenant.CovenantStatus.FORGED && cov.status != Covenant.CovenantStatus.ACCEPTED) {
                        throw CovenantException.conflict("CANNOT_CANCEL", "Cannot cancel covenant once funds are sealed", "status");
                    }

                    cov.status = Covenant.CovenantStatus.CANCELLED;
                    cov.cancelledAt = Instant.now();

                    return covenantRepo.persist(cov)
                            .map(CovenantResponse::from);
                });
    }

    // ─── 8. QUERY COVENANTS ───────────────────────────────────────────────────

    public Uni<List<CovenantResponse>> getPilgrimCovenants(UUID pilgrimId, int page, int size) {
        return covenantRepo.findByPilgrimId(pilgrimId, page, size)
                .map(list -> list.stream().map(CovenantResponse::from).toList());
    }

    public Uni<CovenantResponse> getCovenant(UUID covenantId) {
        return covenantRepo.findByIdSafe(covenantId)
                .map(opt -> opt.map(CovenantResponse::from)
                        .orElseThrow(() -> CovenantException.notFound("COVENANT_NOT_FOUND", "Covenant room not found")));
    }

    // ─── HELPER ───────────────────────────────────────────────────────────────

    private String generateInvitationCode() {
        StringBuilder sb = new StringBuilder(12);
        for (int i = 0; i < 12; i++) {
            sb.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }
}
