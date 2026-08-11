package com.eyesofpriestess.auth.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Pilgrim — The core identity entity of EyesOfPriestess.
 * Single source of truth for all attuned souls in the Sanctum Network.
 * Schema: seal.pilgrims
 */
@Entity
@Table(schema = "auth", name = "pilgrims")
public class Pilgrim extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "phone", nullable = false, unique = true, length = 15)
    public String phone;

    @Column(name = "email", unique = true, length = 255)
    public String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    public String passwordHash;

    @Column(name = "pin_hash", nullable = false, length = 255)
    public String pinHash;

    @Column(name = "full_name", nullable = false, length = 100)
    public String fullName;

    @Column(name = "profile_photo_url", length = 500)
    public String profilePhotoUrl;

    // ─── Attunement (KYC) ────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "attunement_status", nullable = false, length = 20)
    public AttunementStatus attunementStatus = AttunementStatus.UNATTUNED;

    @Column(name = "attunement_id_hash", length = 255)
    public String attunementIdHash;

    @Column(name = "attunement_verified_at")
    public Instant attunementVerifiedAt;

    // ─── Roles ────────────────────────────────────────────────────────────────

    @Column(name = "is_oracle", nullable = false)
    public boolean isOracle = false;

    // ─── Status & Security ────────────────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public PilgrimStatus status = PilgrimStatus.ATTUNED;

    @Column(name = "pin_failed_attempts", nullable = false)
    public int pinFailedAttempts = 0;

    @Column(name = "pin_locked_until")
    public Instant pinLockedUntil;

    @Column(name = "login_failed_attempts", nullable = false)
    public int loginFailedAttempts = 0;

    @Column(name = "login_locked_until")
    public Instant loginLockedUntil;

    // ─── Covenant Metrics ─────────────────────────────────────────────────────

    @Column(name = "covenant_score", nullable = false, precision = 2, scale = 1)
    public BigDecimal covenantScore = new BigDecimal("5.0");

    @Column(name = "total_covenants", nullable = false)
    public int totalCovenants = 0;

    @Column(name = "fulfilled_covenants", nullable = false)
    public int fulfilledCovenants = 0;

    @Column(name = "judgment_count", nullable = false)
    public int judgmentCount = 0;

    // ─── Timestamps ───────────────────────────────────────────────────────────

    @CreationTimestamp
    @Column(name = "attuned_at", nullable = false, updatable = false)
    public Instant attunedAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @Column(name = "last_rite_at")
    public Instant lastRiteAt;

    // ─── Enums ────────────────────────────────────────────────────────────────

    public enum AttunementStatus {
        UNATTUNED, PENDING, ATTUNED, REJECTED
    }

    public enum PilgrimStatus {
        ATTUNED, SUSPENDED, SANCTIONED
    }

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static Pilgrim forge(String phone, String email, String fullName,
                                String passwordHash, String pinHash) {
        Pilgrim p = new Pilgrim();
        p.phone = phone;
        p.email = email;
        p.fullName = fullName;
        p.passwordHash = passwordHash;
        p.pinHash = pinHash;
        return p;
    }

    public boolean isSanctioned() {
        return this.status == PilgrimStatus.SANCTIONED;
    }

    public boolean isPinLocked() {
        return this.pinLockedUntil != null && Instant.now().isBefore(this.pinLockedUntil);
    }

    public boolean isLoginLocked() {
        return this.loginLockedUntil != null && Instant.now().isBefore(this.loginLockedUntil);
    }
}
