package com.eyesofpriestess.seal.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * SealResponse — Response for authentication endpoints (login, refresh).
 * Contains access seal and refresh seal (JWTs).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SealResponse {

    public String accessSeal;
    public String refreshSeal;
    public long expiresIn;       // seconds until access seal expires
    public String sealType = "Sacred";
    public PilgrimResponse pilgrim;

    // For refresh-only response (no pilgrim needed)
    public static SealResponse refreshed(String accessSeal, long expiresIn) {
        SealResponse r = new SealResponse();
        r.accessSeal = accessSeal;
        r.expiresIn = expiresIn;
        return r;
    }

    public static SealResponse issued(String accessSeal, String refreshSeal,
                                      long expiresIn, PilgrimResponse pilgrim) {
        SealResponse r = new SealResponse();
        r.accessSeal = accessSeal;
        r.refreshSeal = refreshSeal;
        r.expiresIn = expiresIn;
        r.pilgrim = pilgrim;
        return r;
    }
}
