package com.eyesofpriestess.vault.dto.response;

import com.eyesofpriestess.vault.entity.Treasury;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class TreasuryResponse {
    public UUID id;
    public UUID pilgrimId;
    public BigDecimal availableTreasury;
    public BigDecimal sealedTreasury;
    public BigDecimal totalTreasury;
    public BigDecimal totalOffered;
    public BigDecimal totalWithdrawn;
    public String status;
    public Instant updatedAt;

    public static TreasuryResponse from(Treasury t) {
        TreasuryResponse r = new TreasuryResponse();
        r.id = t.id;
        r.pilgrimId = t.pilgrimId;
        r.availableTreasury = t.availableTreasury;
        r.sealedTreasury = t.sealedTreasury;
        r.totalTreasury = t.totalTreasury();
        r.totalOffered = t.totalOffered;
        r.totalWithdrawn = t.totalWithdrawn;
        r.status = t.status.name();
        r.updatedAt = t.updatedAt;
        return r;
    }
}
