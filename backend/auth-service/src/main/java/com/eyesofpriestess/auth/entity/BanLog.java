package com.eyesofpriestess.auth.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * BanLog — Audit trail for User sanctions.
 * Schema: seal.sanction_records
 */
@Entity
@Table(schema = "auth", name = "sanction_records")
public class BanLog extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "pilgrim_id", nullable = false)
    public UUID userId;

    @Column(name = "sanctioned_by")
    public UUID sanctionedBy;

    @Column(name = "reason", nullable = false, length = 50)
    public String reason; // FRAUDULENT_ACTIVITY | SPAM | HARASSMENT | TERMS_VIOLATION | OTHER

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "sanction_duration", nullable = false, length = 20)
    public String sanctionDuration; // TEMPORARY | ETERNAL

    @Column(name = "sanctioned_until")
    public Instant sanctionedUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "revoked_at")
    public Instant revokedAt;

    @Column(name = "revoked_by")
    public UUID revokedBy;

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static BanLog forge(UUID userId, UUID sanctionedBy,
                                       String reason, String description,
                                       String sanctionDuration, Instant sanctionedUntil) {
        BanLog r = new BanLog();
        r.userId = userId;
        r.sanctionedBy = sanctionedBy;
        r.reason = reason;
        r.description = description;
        r.sanctionDuration = sanctionDuration;
        r.sanctionedUntil = sanctionedUntil;
        return r;
    }
}
