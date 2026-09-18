package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * LoginRequest — Payload for POST /seal/rite (Login).
 */
public class LoginRequest {

    public String phone;
    public String email;
    public String identifier;

    @NotBlank(message = "Password must not be empty")
    public String password;

    /** Device ID for session tracking. */
    public String deviceId;

    public String getResolvedIdentifier() {
        if (phone != null && !phone.isBlank()) return phone.trim();
        if (email != null && !email.isBlank()) return email.trim();
        if (identifier != null && !identifier.isBlank()) return identifier.trim();
        return "";
    }

    public boolean isEmail() {
        return getResolvedIdentifier().contains("@");
    }
}
