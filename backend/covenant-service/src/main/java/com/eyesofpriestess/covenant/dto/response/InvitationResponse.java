package com.eyesofpriestess.covenant.dto.response;

import com.eyesofpriestess.covenant.entity.CovenantInvitation;

import java.time.Instant;
import java.util.UUID;

public class InvitationResponse {
    public UUID id;
    public UUID covenantId;
    public String invitationCode;
    public boolean isClaimed;
    public Instant expiresAt;

    public static InvitationResponse from(CovenantInvitation inv) {
        InvitationResponse r = new InvitationResponse();
        r.id = inv.id;
        r.covenantId = inv.covenantId;
        r.invitationCode = inv.invitationCode;
        r.isClaimed = inv.isClaimed;
        r.expiresAt = inv.expiresAt;
        return r;
    }
}
