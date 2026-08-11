package com.eyesofpriestess.room.dto.response;

import com.eyesofpriestess.room.entity.Room;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class RoomResponse {
    public UUID id;
    public String title;
    public String description;
    public UUID initiatorId;
    public String initiatorRole;
    public UUID counterpartyId;
    public UUID buyerId;
    public UUID sellerId;
    public BigDecimal amount;
    public String status;
    public Integer autoReleaseHours;
    public Instant expiresAt;
    public Instant sealedAt;
    public Instant deliveredAt;
    public Instant fulfilledAt;
    public Instant disputedAt;
    public Instant cancelledAt;
    public Instant autoReleaseAt;
    public Instant createdAt;
    public Instant updatedAt;
    public String invitationCode;

    public static RoomResponse from(Room c) {
        RoomResponse r = new RoomResponse();
        r.id = c.id;
        r.title = c.title;
        r.description = c.description;
        r.initiatorId = c.initiatorId;
        r.initiatorRole = c.initiatorRole;
        r.counterpartyId = c.counterpartyId;
        r.buyerId = c.buyerId;
        r.sellerId = c.sellerId;
        r.amount = c.amount;
        r.status = c.status.name();
        r.autoReleaseHours = c.autoReleaseHours;
        r.expiresAt = c.expiresAt;
        r.sealedAt = c.sealedAt;
        r.deliveredAt = c.deliveredAt;
        r.fulfilledAt = c.fulfilledAt;
        r.disputedAt = c.disputedAt;
        r.cancelledAt = c.cancelledAt;
        r.autoReleaseAt = c.autoReleaseAt;
        r.createdAt = c.createdAt;
        r.updatedAt = c.updatedAt;
        return r;
    }
}
