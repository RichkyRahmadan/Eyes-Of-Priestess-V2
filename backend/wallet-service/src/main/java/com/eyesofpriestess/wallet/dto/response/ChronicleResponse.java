package com.eyesofpriestess.wallet.dto.response;

import com.eyesofpriestess.wallet.entity.Chronicle;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class ChronicleResponse {
    public UUID id;
    public UUID treasuryId;
    public String type;
    public BigDecimal amount;
    public BigDecimal tithe;
    public BigDecimal netAmount;
    public String status;
    public String referenceId;
    public String referenceType;
    public UUID counterpartyTreasuryId;
    public String counterpartyName;
    public String description;
    public Map<String, Object> metadata;
    public BigDecimal treasuryBefore;
    public BigDecimal treasuryAfter;
    public Instant createdAt;
    public Instant completedAt;

    public static ChronicleResponse from(Chronicle c) {
        ChronicleResponse r = new ChronicleResponse();
        r.id = c.id;
        r.treasuryId = c.treasuryId;
        r.type = c.type;
        r.amount = c.amount;
        r.tithe = c.tithe;
        r.netAmount = c.netAmount;
        r.status = c.status.name();
        r.referenceId = c.referenceId;
        r.referenceType = c.referenceType;
        r.counterpartyTreasuryId = c.counterpartyTreasuryId;
        r.counterpartyName = c.counterpartyName;
        r.description = c.description;
        r.metadata = c.metadata;
        r.treasuryBefore = c.treasuryBefore;
        r.treasuryAfter = c.treasuryAfter;
        r.createdAt = c.createdAt;
        r.completedAt = c.completedAt;
        return r;
    }
}
