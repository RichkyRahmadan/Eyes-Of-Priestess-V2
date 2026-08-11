package com.eyesofpriestess.wallet.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Wallet — The User's financial vessel.
 * Tracks available_treasury (spendable) and sealed_treasury (held in escrow).
 * Uses optimistic locking (@Version) to prevent concurrent balance corruption.
 * Schema: vault.wallets
 */
@Entity
@Table(schema = "wallet", name = "wallets")
public class Wallet extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "pilgrim_id", nullable = false, unique = true)
    public UUID userId;

    @Column(name = "available_treasury", nullable = false, precision = 15, scale = 2)
    public BigDecimal availableTreasury = BigDecimal.ZERO;

    @Column(name = "sealed_treasury", nullable = false, precision = 15, scale = 2)
    public BigDecimal sealedTreasury = BigDecimal.ZERO;

    @Column(name = "total_offered", nullable = false, precision = 15, scale = 2)
    public BigDecimal totalOffered = BigDecimal.ZERO;

    @Column(name = "total_withdrawn", nullable = false, precision = 15, scale = 2)
    public BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public TreasuryStatus status = TreasuryStatus.ACTIVE;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt = Instant.now();

    @Version
    @Column(name = "version", nullable = false)
    public long version = 0;

    public enum TreasuryStatus {
        ACTIVE, FROZEN, SUSPENDED
    }

    public BigDecimal totalTreasury() {
        return availableTreasury.add(sealedTreasury);
    }

    public boolean canDebit(BigDecimal amount) {
        return availableTreasury.compareTo(amount) >= 0
                && status == TreasuryStatus.ACTIVE;
    }

    // Factory
    public static Wallet forPilgrim(UUID userId) {
        Wallet t = new Wallet();
        t.userId = userId;
        return t;
    }
}
