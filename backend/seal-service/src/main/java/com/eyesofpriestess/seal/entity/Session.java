package com.eyesofpriestess.seal.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Session — Tracks active Sacred Seals (refresh tokens) for revocation.
 * Schema: seal.sessions
 */
@Entity
@Table(schema = "seal", name = "sessions")
public class Session extends PanacheEntityBase {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, updatable = false)
    public UUID id;

    @Column(name = "pilgrim_id", nullable = false)
    public UUID pilgrimId;

    @Column(name = "refresh_seal_jti", nullable = false, unique = true, length = 255)
    public String refreshSealJti;

    @Column(name = "device_id", length = 255)
    public String deviceId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "device_info", columnDefinition = "jsonb")
    public Map<String, Object> deviceInfo;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "severed_at")
    public Instant severedAt;

    @Column(name = "severed_reason", length = 50)
    public String severedReason; // SEVERED | SANCTIONED | PASSWORD_CHANGED

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static Session forge(UUID pilgrimId, String refreshSealJti, String deviceId, Instant expiresAt) {
        Session s = new Session();
        s.pilgrimId = pilgrimId;
        s.refreshSealJti = refreshSealJti;
        s.deviceId = deviceId;
        s.expiresAt = expiresAt;
        return s;
    }

    public boolean isActive() {
        return severedAt == null && Instant.now().isBefore(expiresAt);
    }

    public void sever(String reason) {
        this.severedAt = Instant.now();
        this.severedReason = reason;
    }
}
