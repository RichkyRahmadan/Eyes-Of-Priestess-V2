package com.eyesofpriestess.room.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Room — Escrow Room entity.
 * Represents a sacred escrow agreement between two users (Buyer & Seller).
 *
 * State Machine:
 * FORGED -> ACCEPTED -> SEALED -> DELIVERED -> FULFILLED
 *   |          |           |          |
 * CANCELLED CANCELLED   DISPUTED   DISPUTED
 */
@Entity
@Table(name = "rooms", schema = "room")
public class Room extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "title", nullable = false)
    public String title;

    @Column(name = "description", columnDefinition = "TEXT")
    public String description;

    @Column(name = "initiator_id", nullable = false)
    public UUID initiatorId;

    @Column(name = "initiator_role", nullable = false, length = 20)
    public String initiatorRole; // BUYER | SELLER

    @Column(name = "counterparty_id")
    public UUID counterpartyId;

    @Column(name = "buyer_id")
    public UUID buyerId;

    @Column(name = "seller_id")
    public UUID sellerId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    public BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    public CovenantStatus status = CovenantStatus.FORGED;

    @Column(name = "auto_release_hours")
    public Integer autoReleaseHours = 48; // Default 48h after delivery

    @Column(name = "expires_at")
    public Instant expiresAt;

    @Column(name = "sealed_at")
    public Instant sealedAt;

    @Column(name = "delivered_at")
    public Instant deliveredAt;

    @Column(name = "fulfilled_at")
    public Instant fulfilledAt;

    @Column(name = "disputed_at")
    public Instant disputedAt;

    @Column(name = "cancelled_at")
    public Instant cancelledAt;

    @Column(name = "auto_release_at")
    public Instant autoReleaseAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt = Instant.now();

    @Version
    public Long version;

    public enum CovenantStatus {
        FORGED,     // Created, awaiting counterparty
        ACCEPTED,   // Counterparty joined
        SEALED,     // Buyer deposited funds into escrow
        DELIVERED,  // Seller completed delivery
        FULFILLED,  // Buyer confirmed or auto-released -> funds paid out
        DISPUTED,   // Dispute raised -> case opened in Judgment Sanctum
        RESOLVED,   // Judgment Sanctum resolved dispute
        CANCELLED,  // Cancelled before sealing
        EXPIRED     // Unaccepted invitation expired
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
