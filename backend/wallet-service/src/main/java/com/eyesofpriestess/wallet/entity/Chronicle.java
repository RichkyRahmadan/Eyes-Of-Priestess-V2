package com.eyesofpriestess.wallet.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Chronicle — Immutable transaction record for every treasury movement.
 * The sacred audit log — once created, never modified.
 * Schema: vault.chronicles
 */
@Entity
@Table(schema = "wallet", name = "chronicles")
public class Chronicle extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "treasury_id", nullable = false)
    public UUID treasuryId;

    @Column(name = "type", nullable = false, length = 30)
    public String type;
    // OFFERING | WITHDRAWAL | TITHING_IN | TITHING_OUT
    // SEAL_HOLD | SEAL_RELEASE | SEAL_REFUND | TITHE

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    public BigDecimal amount;

    @Column(name = "tithe", nullable = false, precision = 15, scale = 2)
    public BigDecimal tithe = BigDecimal.ZERO;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    public BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public ChronicleStatus status = ChronicleStatus.PENDING;

    @Column(name = "reference_id", length = 100)
    public String referenceId;

    @Column(name = "reference_type", length = 30)
    public String referenceType;

    @Column(name = "counterparty_treasury_id")
    public UUID counterpartyTreasuryId;

    @Column(name = "counterparty_name", length = 100)
    public String counterpartyName;

    @Column(name = "description", length = 255)
    public String description;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    public Map<String, Object> metadata;

    @Column(name = "treasury_before", nullable = false, precision = 15, scale = 2)
    public BigDecimal treasuryBefore;

    @Column(name = "treasury_after", nullable = false, precision = 15, scale = 2)
    public BigDecimal treasuryAfter;

    @Column(name = "covenant_key", length = 100, unique = true)
    public String covenantKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "completed_at")
    public Instant completedAt;

    public enum ChronicleStatus {
        PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    }

    // Factory
    public static Chronicle record(UUID treasuryId, String type, BigDecimal amount,
                                   BigDecimal tithe, BigDecimal before, BigDecimal after) {
        Chronicle c = new Chronicle();
        c.treasuryId = treasuryId;
        c.type = type;
        c.amount = amount;
        c.tithe = tithe;
        c.netAmount = amount.subtract(tithe);
        c.treasuryBefore = before;
        c.treasuryAfter = after;
        c.status = ChronicleStatus.COMPLETED;
        c.completedAt = Instant.now();
        return c;
    }
}
