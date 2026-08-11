package com.eyesofpriestess.auth.dto.response;

import com.eyesofpriestess.auth.entity.User;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * UserResponse — Public-safe representation of a User.
 * Excludes: password_hash, pin_hash, attunement_id_hash (shown masked).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    public UUID id;
    public String phone;
    public String email;
    public String fullName;
    public String profilePhoto;
    public String attunementStatus;
    public String attunementIdHash;   // masked if present: "3175********1234"
    public Instant attunedAt;
    public boolean isSanctioned;
    public boolean isOracle;
    public BigDecimal covenantScore;
    public int totalCovenants;
    public int fulfilledCovenants;
    public int judgmentCount;
    public Instant lastRiteAt;

    // ─── Factory ──────────────────────────────────────────────────────────────

    public static UserResponse from(User p) {
        UserResponse r = new UserResponse();
        r.id = p.id;
        r.phone = p.phone;
        r.email = p.email;
        r.fullName = p.fullName;
        r.profilePhoto = p.profilePhotoUrl;
        r.attunementStatus = p.attunementStatus.name().toLowerCase();
        r.attunedAt = p.attunedAt;
        r.isSanctioned = p.isSanctioned();
        r.isOracle = p.isOracle;
        r.covenantScore = p.covenantScore;
        r.totalCovenants = p.totalCovenants;
        r.fulfilledCovenants = p.fulfilledCovenants;
        r.judgmentCount = p.judgmentCount;
        r.lastRiteAt = p.lastRiteAt;

        // Mask attunement ID hash if present
        if (p.attunementIdHash != null && p.attunementIdHash.length() > 8) {
            r.attunementIdHash = p.attunementIdHash.substring(0, 4)
                + "********"
                + p.attunementIdHash.substring(p.attunementIdHash.length() - 4);
        }

        return r;
    }
}
