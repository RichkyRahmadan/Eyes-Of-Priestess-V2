package com.eyesofpriestess.dispute.service;

import com.eyesofpriestess.dispute.client.WalletClient;
import com.eyesofpriestess.dispute.dto.request.ResolveDisputeRequest;
import com.eyesofpriestess.dispute.dto.request.SubmitEvidenceRequest;
import com.eyesofpriestess.dispute.dto.response.DisputeCaseResponse;
import com.eyesofpriestess.dispute.entity.DisputeCase;
import com.eyesofpriestess.dispute.event.DisputeResolvedEvent;
import com.eyesofpriestess.dispute.exception.DisputeException;
import com.eyesofpriestess.dispute.repository.DisputeCaseRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DisputeService — Core dispute resolution engine.
 *
 * Responsibilities:
 * - Creating a DisputeCase when Room-broken event arrives
 * - Allowing both parties to submit evidence
 * - Oracle assigns a case (moves to DELIBERATING)
 * - Oracle renders a verdict → triggers Vault Sanctum financial settlement
 * - Emits judgment-resolved event for downstream services
 */
@ApplicationScoped
public class DisputeService {

    private static final Logger LOG = Logger.getLogger(DisputeService.class);

    @Inject DisputeCaseRepository caseRepo;

    @Inject
    @RestClient
    WalletClient WalletClient;

    @Inject
    @Channel("judgment-resolved")
    Emitter<DisputeResolvedEvent> resolvedEmitter;

    // ─── 1. OPEN CASE (called by event consumer) ──────────────────────────────

    @WithTransaction
    public Uni<DisputeCaseResponse> openCase(UUID roomId, UUID initiatorId, UUID counterpartyId,
                                               String reason, String description, String evidenceUrl) {
        // Idempotency: avoid duplicate cases
        return caseRepo.findByCovenantId(roomId)
                .flatMap(existing -> {
                    if (existing.isPresent()) {
                        LOG.warnf("[JUDGMENT] Case already exists for Room=%s", roomId);
                        return Uni.createFrom().item(DisputeCaseResponse.from(existing.get()));
                    }

                    DisputeCase c = new DisputeCase();
                    c.roomId = roomId;
                    c.initiatorId = initiatorId;
                    c.counterpartyId = counterpartyId;
                    c.reason = reason;
                    c.description = description;
                    c.initiatorEvidenceUrl = evidenceUrl;
                    c.status = DisputeCase.CaseStatus.OPEN;

                    return caseRepo.persist(c)
                            .map(saved -> {
                                LOG.infof("[JUDGMENT] Case opened: id=%s Room=%s", saved.id, roomId);
                                return DisputeCaseResponse.from(saved);
                            });
                });
    }

    // ─── 2. SUBMIT EVIDENCE ───────────────────────────────────────────────────

    @WithTransaction
    public Uni<DisputeCaseResponse> submitEvidence(UUID caseId, UUID userId, SubmitEvidenceRequest req) {
        return caseRepo.findByIdSafe(caseId)
                .flatMap(opt -> {
                    DisputeCase c = opt.orElseThrow(() ->
                            DisputeException.notFound("CASE_NOT_FOUND", "Judgment case not found"));

                    if (c.status == DisputeCase.CaseStatus.RENDERED) {
                        throw DisputeException.conflict("CASE_CLOSED", "Cannot submit evidence after judgment is rendered", "status");
                    }

                    if (userId.equals(c.initiatorId)) {
                        c.initiatorEvidenceUrl = req.evidenceUrl;
                    } else if (userId.equals(c.counterpartyId)) {
                        c.counterpartyEvidenceUrl = req.evidenceUrl;
                    } else {
                        throw DisputeException.forbidden("NOT_PARTY_TO_CASE", "Only case parties can submit evidence");
                    }

                    return caseRepo.persist(c).map(DisputeCaseResponse::from);
                });
    }

    // ─── 3. ASSIGN (Oracle takes case) ────────────────────────────────────────

    @WithTransaction
    public Uni<DisputeCaseResponse> assignCase(UUID caseId, UUID oracleId) {
        return caseRepo.findByIdSafe(caseId)
                .flatMap(opt -> {
                    DisputeCase c = opt.orElseThrow(() ->
                            DisputeException.notFound("CASE_NOT_FOUND", "Judgment case not found"));

                    if (c.status != DisputeCase.CaseStatus.OPEN) {
                        throw DisputeException.conflict("INVALID_STATE", "Case is not in OPEN status", "status");
                    }

                    c.oracleId = oracleId;
                    c.status = DisputeCase.CaseStatus.DELIBERATING;
                    c.deliberatingAt = Instant.now();

                    return caseRepo.persist(c)
                            .map(saved -> {
                                LOG.infof("[JUDGMENT] Case %s assigned to Oracle %s", caseId, oracleId);
                                return DisputeCaseResponse.from(saved);
                            });
                });
    }

    // ─── 4. RENDER JUDGMENT (Oracle delivers verdict) ─────────────────────────

    @WithTransaction
    public Uni<DisputeCaseResponse> renderJudgment(UUID caseId, UUID oracleId, ResolveDisputeRequest req,
                                                      UUID buyerId, UUID sellerId, BigDecimal amount) {
        return caseRepo.findByIdSafe(caseId)
                .flatMap(opt -> {
                    DisputeCase c = opt.orElseThrow(() ->
                            DisputeException.notFound("CASE_NOT_FOUND", "Judgment case not found"));

                    if (c.status != DisputeCase.CaseStatus.DELIBERATING) {
                        throw DisputeException.conflict("INVALID_STATE",
                                "Judgment can only be rendered when case is DELIBERATING", "status");
                    }

                    if (!oracleId.equals(c.oracleId)) {
                        throw DisputeException.forbidden("NOT_ASSIGNED_ORACLE", "Only the assigned Oracle can render this judgment");
                    }

                    if (req.resolutionType == DisputeCase.ResolutionType.SPLIT) {
                        if (req.splitPercentage == null) {
                            throw DisputeException.badRequest("SPLIT_PCT_REQUIRED", "splitPercentage is required for SPLIT resolution");
                        }
                        c.splitPercentage = req.splitPercentage;
                    }

                    c.resolutionType = req.resolutionType;
                    c.oracleNotes = req.oracleNotes;
                    c.status = DisputeCase.CaseStatus.RENDERED;
                    c.renderedAt = Instant.now();

                    return caseRepo.persist(c)
                            .flatMap(saved -> executeSettlement(saved, buyerId, sellerId, amount));
                });
    }

    // ─── 5. QUERY CASES ───────────────────────────────────────────────────────

    @WithSession
    public Uni<List<DisputeCaseResponse>> listAllCases(int page, int size) {
        return caseRepo.findAllPaged(page, size)
                .map(list -> list.stream().map(DisputeCaseResponse::from).toList());
    }

    @WithSession
    public Uni<List<DisputeCaseResponse>> listOpenCases(int page, int size) {
        return caseRepo.findByStatus(DisputeCase.CaseStatus.OPEN, page, size)
                .map(list -> list.stream().map(DisputeCaseResponse::from).toList());
    }

    @WithSession
    public Uni<List<DisputeCaseResponse>> listDeliberatingCases(int page, int size) {
        return caseRepo.findByStatus(DisputeCase.CaseStatus.DELIBERATING, page, size)
                .map(list -> list.stream().map(DisputeCaseResponse::from).toList());
    }

    @WithSession
    public Uni<DisputeCaseResponse> getCase(UUID caseId) {
        return caseRepo.findByIdSafe(caseId)
                .map(opt -> opt.map(DisputeCaseResponse::from)
                        .orElseThrow(() -> DisputeException.notFound("CASE_NOT_FOUND", "Judgment case not found")));
    }

    @WithSession
    public Uni<List<DisputeCaseResponse>> getMyCases(UUID userId) {
        return caseRepo.findByPilgrimId(userId)
                .map(list -> list.stream().map(DisputeCaseResponse::from).toList());
    }

    // ─── INTERNAL: Financial Settlement Execution ─────────────────────────────

    private Uni<DisputeCaseResponse> executeSettlement(DisputeCase c, UUID buyerId, UUID sellerId, BigDecimal amount) {
        return switch (c.resolutionType) {
            case RELEASE_TO_COUNTERPART ->
                    WalletClient.releaseEscrow(buyerId, sellerId, amount, c.roomId.toString())
                            .map(res -> {
                                emitResolved(c, buyerId, sellerId, amount);
                                return DisputeCaseResponse.from(c);
                            });

            case REFUND_TO_INITIATOR ->
                    WalletClient.refundEscrow(buyerId, amount, c.roomId.toString())
                            .map(res -> {
                                emitResolved(c, buyerId, sellerId, amount);
                                return DisputeCaseResponse.from(c);
                            });

            case SPLIT -> {
                // sellerPct from splitPercentage (e.g. 60 = 60% to seller)
                int sellerPct = c.splitPercentage;
                yield WalletClient.splitEscrow(buyerId, sellerId, amount, c.roomId.toString(), sellerPct)
                        .map(res -> {
                            emitResolved(c, buyerId, sellerId, amount);
                            return DisputeCaseResponse.from(c);
                        });
            }
        };
    }

    private void emitResolved(DisputeCase c, UUID buyerId, UUID sellerId, BigDecimal amount) {
        resolvedEmitter.send(new DisputeResolvedEvent(
                c.id.toString(), c.roomId.toString(),
                buyerId.toString(), sellerId.toString(),
                c.resolutionType.name(), amount, c.splitPercentage
        ));
        LOG.infof("[JUDGMENT] judgment-resolved event emitted for case=%s resolution=%s", c.id, c.resolutionType);
    }
}
