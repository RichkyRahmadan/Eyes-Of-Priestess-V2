package com.eyesofpriestess.dispute.dto.response;

import com.eyesofpriestess.dispute.entity.DisputeCase;

import java.time.Instant;
import java.util.UUID;

public class DisputeCaseResponse {
    public UUID id;
    public UUID roomId;
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

    public static DisputeCaseResponse from(DisputeCase c) {
        DisputeCaseResponse r = new DisputeCaseResponse();
        r.id = c.id;
        r.roomId = c.roomId;
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
