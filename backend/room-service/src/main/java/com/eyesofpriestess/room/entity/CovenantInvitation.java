package com.eyesofpriestess.room.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * CovenantInvitation — Unique invitation key/link to join a Covenant.
 */
@Entity
@Table(name = "covenant_invitations", schema = "room")
public class CovenantInvitation extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "covenant_id", nullable = false)
    public UUID covenantId;

    @Column(name = "invitation_code", nullable = false, unique = true, length = 64)
    public String invitationCode;

    @Column(name = "claimed_by")
    public UUID claimedBy;

    @Column(name = "is_claimed", nullable = false)
    public boolean isClaimed = false;

    @Column(name = "expires_at", nullable = false)
    public Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt = Instant.now();
}
