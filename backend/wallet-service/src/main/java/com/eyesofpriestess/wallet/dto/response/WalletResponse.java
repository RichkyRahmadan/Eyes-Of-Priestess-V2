package com.eyesofpriestess.wallet.dto.response;

import com.eyesofpriestess.wallet.entity.Wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class WalletResponse {
    public UUID id;
    public UUID userId;
    public BigDecimal availableTreasury;
    public BigDecimal sealedTreasury;
    public BigDecimal totalTreasury;
    public BigDecimal totalOffered;
    public BigDecimal totalWithdrawn;
    public String status;
    public Instant updatedAt;

    public static WalletResponse from(Wallet t) {
        WalletResponse r = new WalletResponse();
        r.id = t.id;
        r.userId = t.userId;
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
