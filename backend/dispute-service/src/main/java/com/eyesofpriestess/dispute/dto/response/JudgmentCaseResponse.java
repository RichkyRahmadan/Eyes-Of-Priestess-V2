package com.eyesofpriestess.dispute.dto.response;

import com.eyesofpriestess.dispute.entity.JudgmentCase;

import java.time.Instant;
import java.util.UUID;

public class JudgmentCaseResponse {
    public UUID id;
    public UUID covenantId;
    public UUID initiatorId;
    public UUID counterpartyId;
    public String reason;
    public String description;
    public String initiatorEvidenceUrl;
    public String counterpartyEvidenceUrl;
    public String status;
    public String resolutionType;
    public UUID oracleId;
    public String oracleNotes;
    public Integer splitPercentage;
    public Instant openedAt;
    public Instant deliberatingAt;
    public Instant renderedAt;
    public Instant updatedAt;

    public static JudgmentCaseResponse from(JudgmentCase c) {
        JudgmentCaseResponse r = new JudgmentCaseResponse();
        r.id = c.id;
        r.covenantId = c.covenantId;
        r.initiatorId = c.initiatorId;
        r.counterpartyId = c.counterpartyId;
        r.reason = c.reason;
        r.description = c.description;
        r.initiatorEvidenceUrl = c.initiatorEvidenceUrl;
        r.counterpartyEvidenceUrl = c.counterpartyEvidenceUrl;
        r.status = c.status.name();
        r.resolutionType = c.resolutionType != null ? c.resolutionType.name() : null;
        r.oracleId = c.oracleId;
        r.oracleNotes = c.oracleNotes;
        r.splitPercentage = c.splitPercentage;
        r.openedAt = c.openedAt;
        r.deliberatingAt = c.deliberatingAt;
        r.renderedAt = c.renderedAt;
        r.updatedAt = c.updatedAt;
        return r;
    }
}
