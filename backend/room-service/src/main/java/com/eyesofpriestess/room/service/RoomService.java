package com.eyesofpriestess.room.service;

import com.eyesofpriestess.room.client.WalletClient;
import com.eyesofpriestess.room.dto.request.*;
import com.eyesofpriestess.room.dto.response.*;
import com.eyesofpriestess.room.entity.*;
import com.eyesofpriestess.room.event.RoomBrokenEvent;
import com.eyesofpriestess.room.event.RoomCompletedEvent;
import com.eyesofpriestess.room.exception.RoomException;
import com.eyesofpriestess.room.repository.*;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
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
 * RoomService — Core Escrow Room state machine engine.
 *
 * Manages:
 * - Room creation & invitation code generation
 * - Acceptance by counterparty & role binding (Buyer/Seller)
 * - Escrow deposit & hold via Vault Sanctum
 * - Delivery declaration by Seller
 * - Fulfillment confirmation & payout release
 * - Dispute initiation & Cancellation
 */
@ApplicationScoped
public class RoomService {

    private static final Logger LOG = Logger.getLogger(RoomService.class);
    private static final String ALPHANUM = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Inject RoomRepository covenantRepo;
    @Inject RoomInvitationRepository invitationRepo;

    @Inject
    @RestClient
    WalletClient WalletClient;

    @Inject
    @Channel("Room-fulfilled")
    Emitter<RoomCompletedEvent> covenantFulfilledEmitter;

    @Inject
    @Channel("Room-broken")
    Emitter<RoomBrokenEvent> covenantBrokenEmitter;

    // ─── 1. FORGE Room (CREATE ESCROW ROOM) ───────────────────────────────

    @WithTransaction
    public Uni<RoomResponse> forgeCovenant(UUID initiatorId, CreateRoomRequest req) {
        Room room = new Room();
        room.title = req.title;
        room.description = req.description;
        room.initiatorId = initiatorId;
        room.initiatorRole = req.getInitiatorRoleNormalized();
        room.amount = req.amount;
        room.roomCode = "ROOM-" + generateInvitationCode().substring(0, 8);
        room.status = Room.CovenantStatus.FORGED;
        room.autoReleaseHours = req.autoReleaseHours != null ? req.autoReleaseHours : 48;
        room.expiresAt = Instant.now().plusSeconds(7 * 86400); // 7 days invitation window

        if ("BUYER".equalsIgnoreCase(req.initiatorRole)) {
            room.buyerId = initiatorId;
        } else {
            room.sellerId = initiatorId;
        }

        return covenantRepo.persist(room)
                .flatMap(saved -> {
                    String code = generateInvitationCode();
                    RoomInvitation inv = new RoomInvitation();
                    inv.roomId = saved.id;
                    inv.invitationCode = code;
                    inv.expiresAt = saved.expiresAt;

                    return invitationRepo.persist(inv)
                            .map(i -> {
                                RoomResponse resp = RoomResponse.from(saved);
                                resp.invitationCode = i.invitationCode;
                                return resp;
                            });
                });
    }

    // ─── 2. ACCEPT Room (JOIN ESCROW ROOM) ────────────────────────────────

    @WithTransaction
    public Uni<RoomResponse> acceptCovenant(UUID counterpartyId, String invitationCode) {
        return invitationRepo.findByCode(invitationCode)
                .flatMap(invOpt -> {
                    RoomInvitation inv = invOpt.orElseThrow(() ->
                            RoomException.notFound("INVITATION_NOT_FOUND", "Invalid invitation code"));

                    if (inv.isClaimed) {
                        throw RoomException.conflict("INVITATION_CLAIMED", "Invitation code has already been claimed", "invitationCode");
                    }

                    if (Instant.now().isAfter(inv.expiresAt)) {
                        throw RoomException.badRequest("INVITATION_EXPIRED", "Invitation code has expired");
                    }

                    return covenantRepo.findByIdSafe(inv.roomId)
                            .flatMap(covOpt -> {
                                Room cov = covOpt.orElseThrow(() ->
                                        RoomException.notFound("COVENANT_NOT_FOUND", "Room room missing"));

                                if (cov.initiatorId.equals(counterpartyId)) {
                                    throw RoomException.badRequest("INVALID_COUNTERPARTY", "Initiator cannot accept their own Room");
                                }

                                if (cov.status != Room.CovenantStatus.FORGED) {
                                    throw RoomException.conflict("INVALID_STATE", "Room is no longer open for acceptance", "status");
                                }

                                cov.counterpartyId = counterpartyId;
                                cov.status = Room.CovenantStatus.ACCEPTED;

                                if ("BUYER".equalsIgnoreCase(cov.initiatorRole)) {
                                    cov.sellerId = counterpartyId;
                                } else {
                                    cov.buyerId = counterpartyId;
                                }

                                inv.isClaimed = true;
                                inv.claimedBy = counterpartyId;

                                return covenantRepo.persist(cov)
                                        .flatMap(c -> invitationRepo.persist(inv).replaceWith(c))
                                        .map(RoomResponse::from);
                            });
                });
    }

    // ─── 3. SEAL Room (DEPOSIT FUNDS INTO ESCROW) ─────────────────────────

    @WithTransaction
    public Uni<RoomResponse> sealCovenant(UUID userId, UUID roomId) {
        return covenantRepo.findByIdSafe(roomId)
                .flatMap(covOpt -> {
                    Room cov = covOpt.orElseThrow(() ->
                            RoomException.notFound("COVENANT_NOT_FOUND", "Room room not found"));

                    if (!userId.equals(cov.buyerId)) {
                        throw RoomException.forbidden("ONLY_BUYER_CAN_SEAL", "Only the designated buyer can deposit funds into escrow");
                    }

                    if (cov.status != Room.CovenantStatus.ACCEPTED) {
                        throw RoomException.conflict("INVALID_STATE", "Room must be ACCEPTED before sealing", "status");
                    }

                    // Invoke Vault Sanctum to lock funds
                    return WalletClient.holdEscrow(cov.buyerId, cov.amount, cov.id.toString())
                            .flatMap(res -> {
                                if (!res.success) {
                                    throw RoomException.badRequest("ESCROW_HOLD_FAILED",
                                            res.error != null ? res.error.message : "Failed to hold funds in Vault");
                                }

                                cov.status = Room.CovenantStatus.SEALED;
                                cov.sealedAt = Instant.now();

                                return covenantRepo.persist(cov)
                                        .map(RoomResponse::from);
                            });
                });
    }

    // ─── 4. DELIVER Room (SELLER MARKS COMPLETED) ─────────────────────────

    @WithTransaction
    public Uni<RoomResponse> deliverCovenant(UUID userId, UUID roomId, DeliverRoomRequest req) {
        return covenantRepo.findByIdSafe(roomId)
                .flatMap(covOpt -> {
                    Room cov = covOpt.orElseThrow(() ->
                            RoomException.notFound("COVENANT_NOT_FOUND", "Room room not found"));

                    if (!userId.equals(cov.sellerId)) {
                        throw RoomException.forbidden("ONLY_SELLER_CAN_DELIVER", "Only the seller can mark delivery");
                    }

                    if (cov.status != Room.CovenantStatus.SEALED) {
                        throw RoomException.conflict("INVALID_STATE", "Room must be SEALED before delivery", "status");
                    }

                    cov.status = Room.CovenantStatus.DELIVERED;
                    cov.deliveredAt = Instant.now();
                    cov.autoReleaseAt = Instant.now().plusSeconds(cov.autoReleaseHours * 3600L);

                    return covenantRepo.persist(cov)
                            .map(RoomResponse::from);
                });
    }

    // ─── 5. FULFILL Room (BUYER CONFIRMS / PAYOUT RELEASE) ────────────────

    @WithTransaction
    public Uni<RoomResponse> fulfillCovenant(UUID userId, UUID roomId) {
        return covenantRepo.findByIdSafe(roomId)
                .flatMap(covOpt -> {
                    Room cov = covOpt.orElseThrow(() ->
                            RoomException.notFound("COVENANT_NOT_FOUND", "Room room not found"));

                    if (cov.status != Room.CovenantStatus.DELIVERED && cov.status != Room.CovenantStatus.SEALED) {
                        throw RoomException.conflict("INVALID_STATE", "Room cannot be fulfilled in status " + cov.status, "status");
                    }

                    if (userId != null && !userId.equals(cov.buyerId)) {
                        throw RoomException.forbidden("ONLY_BUYER_CAN_CONFIRM", "Only the buyer can confirm fulfillment");
                    }

                    // Invoke Vault Sanctum to release payout to seller
                    return WalletClient.releaseEscrow(cov.buyerId, cov.sellerId, cov.amount, cov.id.toString())
                            .flatMap(res -> {
                                cov.status = Room.CovenantStatus.FULFILLED;
                                cov.fulfilledAt = Instant.now();

                                return covenantRepo.persist(cov)
                                        .map(c -> {
                                            covenantFulfilledEmitter.send(new RoomCompletedEvent(
                                                    cov.id.toString(), cov.buyerId.toString(), cov.sellerId.toString(), cov.amount
                                            ));
                                            return RoomResponse.from(c);
                                        });
                            });
                });
    }

    // ─── 6. DISPUTE Room (OPEN JUDGMENT CASE) ─────────────────────────────

    @WithTransaction
    public Uni<RoomResponse> disputeCovenant(UUID userId, UUID roomId, DisputeRoomRequest req) {
        return covenantRepo.findByIdSafe(roomId)
                .flatMap(covOpt -> {
                    Room cov = covOpt.orElseThrow(() ->
                            RoomException.notFound("COVENANT_NOT_FOUND", "Room room not found"));

                    if (!userId.equals(cov.buyerId) && !userId.equals(cov.sellerId)) {
                        throw RoomException.forbidden("NOT_PARTY_TO_COVENANT", "Only Room parties can initiate a dispute");
                    }

                    if (cov.status != Room.CovenantStatus.SEALED && cov.status != Room.CovenantStatus.DELIVERED) {
                        throw RoomException.conflict("INVALID_STATE", "Cannot dispute unsealed or already completed Room", "status");
                    }

                    cov.status = Room.CovenantStatus.DISPUTED;
                    cov.disputedAt = Instant.now();

                    return covenantRepo.persist(cov)
                            .map(c -> {
                                covenantBrokenEmitter.send(new RoomBrokenEvent(
                                        cov.id.toString(), cov.initiatorId.toString(),
                                        cov.counterpartyId != null ? cov.counterpartyId.toString() : "", req.reason
                                ));
                                return RoomResponse.from(c);
                            });
                });
    }

    // ─── 7. CANCEL Room ───────────────────────────────────────────────────

    @WithTransaction
    public Uni<RoomResponse> cancelCovenant(UUID userId, UUID roomId) {
        return covenantRepo.findByIdSafe(roomId)
                .flatMap(covOpt -> {
                    Room cov = covOpt.orElseThrow(() ->
                            RoomException.notFound("COVENANT_NOT_FOUND", "Room room not found"));

                    if (!userId.equals(cov.initiatorId)) {
                        throw RoomException.forbidden("ONLY_INITIATOR_CAN_CANCEL", "Only the initiator can cancel this Room");
                    }

                    if (cov.status != Room.CovenantStatus.FORGED && cov.status != Room.CovenantStatus.ACCEPTED) {
                        throw RoomException.conflict("CANNOT_CANCEL", "Cannot cancel Room once funds are sealed", "status");
                    }

                    cov.status = Room.CovenantStatus.CANCELLED;
                    cov.cancelledAt = Instant.now();

                    return covenantRepo.persist(cov)
                            .map(RoomResponse::from);
                });
    }

    // ─── 8. QUERY rooms ───────────────────────────────────────────────────

    @WithSession
    public Uni<List<RoomResponse>> getPilgrimCovenants(UUID userId, int page, int size) {
        return covenantRepo.findByPilgrimId(userId, page, size)
                .map(list -> list.stream().map(RoomResponse::from).toList());
    }

    @WithSession
    public Uni<RoomResponse> getCovenant(UUID roomId) {
        return covenantRepo.findByIdSafe(roomId)
                .map(opt -> opt.map(RoomResponse::from)
                        .orElseThrow(() -> RoomException.notFound("COVENANT_NOT_FOUND", "Room room not found")));
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
