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
 * Offering — Top-up request via Xendit (VA, QRIS, eWallet).
 * Status lifecycle: PENDING → ACCEPTED | EXPIRED | CANCELLED | FAILED
 * Schema: vault.offerings
 */
@Entity
@Table(schema = "wallet", name = "offerings")
public class Offering extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "treasury_id", nullable = false)
    public UUID treasuryId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    public BigDecimal amount;

    @Column(name = "tithe", nullable = false, precision = 15, scale = 2)
    public BigDecimal tithe = BigDecimal.ZERO;

    @Column(name = "gateway", nullable = false, length = 20)
    public String gateway = "XENDIT";

    @Column(name = "gateway_transaction_id", length = 255)
    public String gatewayTransactionId;

    @Column(name = "payment_method", nullable = false, length = 30)
    public String paymentMethod; // VIRTUAL_ACCOUNT | E_WALLET | QRIS | BANK_TRANSFER

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payment_details", columnDefinition = "jsonb")
    public Map<String, Object> paymentDetails;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public OfferingStatus status = OfferingStatus.PENDING;

    @Column(name = "expires_at")
    public Instant expiresAt;

    @Column(name = "accepted_at")
    public Instant acceptedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "covenant_key", length = 100, unique = true)
    public String covenantKey; // Idempotency key

    public enum OfferingStatus {
        PENDING, ACCEPTED, EXPIRED, CANCELLED, FAILED
    }
}
