package com.eyesofpriestess.wallet.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Withdrawal — Bank disbursement request via Xendit Disbursement API.
 * Status: PENDING → PROCESSING → COMPLETED | FAILED
 * Schema: vault.withdrawals
 */
@Entity
@Table(schema = "wallet", name = "withdrawals")
public class Withdrawal extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "treasury_id", nullable = false)
    public UUID treasuryId;

    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    public BigDecimal amount;

    @Column(name = "tithe", nullable = false, precision = 15, scale = 2)
    public BigDecimal tithe;

    @Column(name = "net_amount", nullable = false, precision = 15, scale = 2)
    public BigDecimal netAmount;

    @Column(name = "bank_code", nullable = false, length = 20)
    public String bankCode;

    @Column(name = "bank_name", nullable = false, length = 100)
    public String bankName;

    @Column(name = "account_number_hash", nullable = false, length = 255)
    public String accountNumberHash; // bcrypt hash for security

    @Column(name = "account_number_masked", nullable = false, length = 20)
    public String accountNumberMasked; // Last 4 digits: ****7890

    @Column(name = "account_name", nullable = false, length = 100)
    public String accountName;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public WithdrawalStatus status = WithdrawalStatus.PENDING;

    @Column(name = "gateway", length = 20)
    public String gateway = "XENDIT";

    @Column(name = "gateway_transaction_id", length = 255)
    public String gatewayTransactionId;

    @Column(name = "processed_at")
    public Instant processedAt;

    @Column(name = "completed_at")
    public Instant completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "covenant_key", length = 100, unique = true)
    public String covenantKey;

    public enum WithdrawalStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }
}
