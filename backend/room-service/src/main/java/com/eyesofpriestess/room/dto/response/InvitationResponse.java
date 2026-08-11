package com.eyesofpriestess.room.dto.response;

import com.eyesofpriestess.room.entity.RoomInvitation;

import java.time.Instant;
import java.util.UUID;

public class InvitationResponse {
    public UUID id;
    public UUID roomId;
    public String invitationCode;
    public boolean isClaimed;
    public Instant expiresAt;

    public static InvitationResponse from(RoomInvitation inv) {
        InvitationResponse r = new InvitationResponse();
        r.id = inv.id;
        r.roomId = inv.roomId;
        r.invitationCode = inv.invitationCode;
        r.isClaimed = inv.isClaimed;
        r.expiresAt = inv.expiresAt;
        return r;
    }
}
