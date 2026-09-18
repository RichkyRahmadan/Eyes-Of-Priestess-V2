package com.eyesofpriestess.wallet.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "bank_accounts", schema = "wallet")
public class BankAccount extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    public UUID id;

    @Column(name = "user_id", nullable = false)
    public UUID userId;

    @Column(name = "bank_code", nullable = false, length = 20)
    public String bankCode;

    @Column(name = "bank_name", nullable = false, length = 100)
    public String bankName;

    @Column(name = "account_number", nullable = false, length = 50)
    public String accountNumber;

    @Column(name = "account_holder_name", nullable = false, length = 100)
    public String accountHolderName;

    @Column(name = "is_primary", nullable = false)
    public boolean isPrimary = false;

    @Column(name = "is_verified", nullable = false)
    public boolean isVerified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "deleted_at")
    public Instant deletedAt;
}
