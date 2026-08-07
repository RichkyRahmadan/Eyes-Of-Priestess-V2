package com.eyesofpriestess.judgment.service;

import com.eyesofpriestess.judgment.client.VaultClient;
import com.eyesofpriestess.judgment.dto.request.RenderJudgmentRequest;
import com.eyesofpriestess.judgment.dto.request.SubmitEvidenceRequest;
import com.eyesofpriestess.judgment.dto.response.JudgmentCaseResponse;
import com.eyesofpriestess.judgment.entity.JudgmentCase;
import com.eyesofpriestess.judgment.event.JudgmentResolvedEvent;
import com.eyesofpriestess.judgment.exception.JudgmentException;
import com.eyesofpriestess.judgment.repository.JudgmentCaseRepository;
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
 * JudgmentService — Core dispute resolution engine.
 *
 * Responsibilities:
 * - Creating a JudgmentCase when covenant-broken event arrives
 * - Allowing both parties to submit evidence
 * - Oracle assigns a case (moves to DELIBERATING)
 * - Oracle renders a verdict → triggers Vault Sanctum financial settlement
 * - Emits judgment-resolved event for downstream services
 */
@ApplicationScoped
public class JudgmentService {

    private static final Logger LOG = Logger.getLogger(JudgmentService.class);

    @Inject JudgmentCaseRepository caseRepo;

    @Inject
    @RestClient
    VaultClient vaultClient;

    @Inject
    @Channel("judgment-resolved")
    Emitter<JudgmentResolvedEvent> resolvedEmitter;

    // ─── 1. OPEN CASE (called by event consumer) ──────────────────────────────

    @WithTransaction
    public Uni<JudgmentCaseResponse> openCase(UUID covenantId, UUID initiatorId, UUID counterpartyId,
                                               String reason, String description, String evidenceUrl) {
        // Idempotency: avoid duplicate cases
        return caseRepo.findByCovenantId(covenantId)
                .flatMap(existing -> {
                    if (existing.isPresent()) {
                        LOG.warnf("[JUDGMENT] Case already exists for covenant=%s", covenantId);
                        return Uni.createFrom().item(JudgmentCaseResponse.from(existing.get()));
                    }

                    JudgmentCase c = new JudgmentCase();
                    c.covenantId = covenantId;
                    c.initiatorId = initiatorId;
                    c.counterpartyId = counterpartyId;
                    c.reason = reason;
                    c.description = description;
                    c.initiatorEvidenceUrl = evidenceUrl;
                    c.status = JudgmentCase.CaseStatus.OPEN;

                    return caseRepo.persist(c)
                            .map(saved -> {
                                LOG.infof("[JUDGMENT] Case opened: id=%s covenant=%s", saved.id, covenantId);
                                return JudgmentCaseResponse.from(saved);
                            });
                });
    }

    // ─── 2. SUBMIT EVIDENCE ───────────────────────────────────────────────────

    @WithTransaction
    public Uni<JudgmentCaseResponse> submitEvidence(UUID caseId, UUID pilgrimId, SubmitEvidenceRequest req) {
        return caseRepo.findByIdSafe(caseId)
                .flatMap(opt -> {
                    JudgmentCase c = opt.orElseThrow(() ->
                            JudgmentException.notFound("CASE_NOT_FOUND", "Judgment case not found"));

                    if (c.status == JudgmentCase.CaseStatus.RENDERED) {
                        throw JudgmentException.conflict("CASE_CLOSED", "Cannot submit evidence after judgment is rendered", "status");
                    }

                    if (pilgrimId.equals(c.initiatorId)) {
                        c.initiatorEvidenceUrl = req.evidenceUrl;
                    } else if (pilgrimId.equals(c.counterpartyId)) {
                        c.counterpartyEvidenceUrl = req.evidenceUrl;
                    } else {
                        throw JudgmentException.forbidden("NOT_PARTY_TO_CASE", "Only case parties can submit evidence");
                    }

                    return caseRepo.persist(c).map(JudgmentCaseResponse::from);
                });
    }

    // ─── 3. ASSIGN (Oracle takes case) ────────────────────────────────────────

    @WithTransaction
    public Uni<JudgmentCaseResponse> assignCase(UUID caseId, UUID oracleId) {
        return caseRepo.findByIdSafe(caseId)
                .flatMap(opt -> {
                    JudgmentCase c = opt.orElseThrow(() ->
                            JudgmentException.notFound("CASE_NOT_FOUND", "Judgment case not found"));

                    if (c.status != JudgmentCase.CaseStatus.OPEN) {
                        throw JudgmentException.conflict("INVALID_STATE", "Case is not in OPEN status", "status");
                    }

                    c.oracleId = oracleId;
                    c.status = JudgmentCase.CaseStatus.DELIBERATING;
                    c.deliberatingAt = Instant.now();

                    return caseRepo.persist(c)
                            .map(saved -> {
                                LOG.infof("[JUDGMENT] Case %s assigned to Oracle %s", caseId, oracleId);
                                return JudgmentCaseResponse.from(saved);
                            });
                });
    }

    // ─── 4. RENDER JUDGMENT (Oracle delivers verdict) ─────────────────────────

    @WithTransaction
    public Uni<JudgmentCaseResponse> renderJudgment(UUID caseId, UUID oracleId, RenderJudgmentRequest req,
                                                      UUID buyerId, UUID sellerId, BigDecimal amount) {
        return caseRepo.findByIdSafe(caseId)
                .flatMap(opt -> {
                    JudgmentCase c = opt.orElseThrow(() ->
                            JudgmentException.notFound("CASE_NOT_FOUND", "Judgment case not found"));

                    if (c.status != JudgmentCase.CaseStatus.DELIBERATING) {
                        throw JudgmentException.conflict("INVALID_STATE",
                                "Judgment can only be rendered when case is DELIBERATING", "status");
                    }

                    if (!oracleId.equals(c.oracleId)) {
                        throw JudgmentException.forbidden("NOT_ASSIGNED_ORACLE", "Only the assigned Oracle can render this judgment");
                    }

                    if (req.resolutionType == JudgmentCase.ResolutionType.SPLIT) {
                        if (req.splitPercentage == null) {
                            throw JudgmentException.badRequest("SPLIT_PCT_REQUIRED", "splitPercentage is required for SPLIT resolution");
                        }
                        c.splitPercentage = req.splitPercentage;
                    }

                    c.resolutionType = req.resolutionType;
                    c.oracleNotes = req.oracleNotes;
                    c.status = JudgmentCase.CaseStatus.RENDERED;
                    c.renderedAt = Instant.now();

                    return caseRepo.persist(c)
                            .flatMap(saved -> executeSettlement(saved, buyerId, sellerId, amount));
                });
    }

    // ─── 5. QUERY CASES ───────────────────────────────────────────────────────

    public Uni<List<JudgmentCaseResponse>> listOpenCases(int page, int size) {
        return caseRepo.findByStatus(JudgmentCase.CaseStatus.OPEN, page, size)
                .map(list -> list.stream().map(JudgmentCaseResponse::from).toList());
    }

    public Uni<List<JudgmentCaseResponse>> listDeliberatingCases(int page, int size) {
        return caseRepo.findByStatus(JudgmentCase.CaseStatus.DELIBERATING, page, size)
                .map(list -> list.stream().map(JudgmentCaseResponse::from).toList());
    }

    public Uni<JudgmentCaseResponse> getCase(UUID caseId) {
        return caseRepo.findByIdSafe(caseId)
                .map(opt -> opt.map(JudgmentCaseResponse::from)
                        .orElseThrow(() -> JudgmentException.notFound("CASE_NOT_FOUND", "Judgment case not found")));
    }

    public Uni<List<JudgmentCaseResponse>> getMyCases(UUID pilgrimId) {
        return caseRepo.findByPilgrimId(pilgrimId)
                .map(list -> list.stream().map(JudgmentCaseResponse::from).toList());
    }

    // ─── INTERNAL: Financial Settlement Execution ─────────────────────────────

    private Uni<JudgmentCaseResponse> executeSettlement(JudgmentCase c, UUID buyerId, UUID sellerId, BigDecimal amount) {
        return switch (c.resolutionType) {
            case RELEASE_TO_COUNTERPART ->
                    vaultClient.releaseEscrow(buyerId, sellerId, amount, c.covenantId.toString())
                            .map(res -> {
                                emitResolved(c, buyerId, sellerId, amount);
                                return JudgmentCaseResponse.from(c);
                            });

            case REFUND_TO_INITIATOR ->
                    vaultClient.refundEscrow(buyerId, amount, c.covenantId.toString())
                            .map(res -> {
                                emitResolved(c, buyerId, sellerId, amount);
                                return JudgmentCaseResponse.from(c);
                            });

            case SPLIT -> {
                // sellerPct from splitPercentage (e.g. 60 = 60% to seller)
                int sellerPct = c.splitPercentage;
                yield vaultClient.splitEscrow(buyerId, sellerId, amount, c.covenantId.toString(), sellerPct)
                        .map(res -> {
                            emitResolved(c, buyerId, sellerId, amount);
                            return JudgmentCaseResponse.from(c);
                        });
            }
        };
    }

    private void emitResolved(JudgmentCase c, UUID buyerId, UUID sellerId, BigDecimal amount) {
        resolvedEmitter.send(new JudgmentResolvedEvent(
                c.id.toString(), c.covenantId.toString(),
                buyerId.toString(), sellerId.toString(),
                c.resolutionType.name(), amount, c.splitPercentage
        ));
        LOG.infof("[JUDGMENT] judgment-resolved event emitted for case=%s resolution=%s", c.id, c.resolutionType);
    }
}
