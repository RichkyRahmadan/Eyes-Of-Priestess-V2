package com.eyesofpriestess.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * AuthResponse — Response for authentication endpoints (login, refresh).
 * Contains access seal and refresh seal (JWTs).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {

    public String accessSeal;
    public String refreshSeal;
    public long expiresIn;       // seconds until access seal expires
    public String sealType = "Sacred";
    public UserResponse User;

    // For refresh-only response (no User needed)
    public static AuthResponse refreshed(String accessSeal, long expiresIn) {
        AuthResponse r = new AuthResponse();
        r.accessSeal = accessSeal;
        r.expiresIn = expiresIn;
        return r;
    }

    public static AuthResponse issued(String accessSeal, String refreshSeal,
                                      long expiresIn, UserResponse User) {
        AuthResponse r = new AuthResponse();
        r.accessSeal = accessSeal;
        r.refreshSeal = refreshSeal;
        r.expiresIn = expiresIn;
        r.User = User;
        return r;
    }
}
