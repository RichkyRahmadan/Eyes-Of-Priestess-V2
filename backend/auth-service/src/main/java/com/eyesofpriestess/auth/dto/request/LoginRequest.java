package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * LoginRequest — Payload for POST /seal/rite (Login).
 */
public class LoginRequest {

    @NotBlank(message = "Phone resonance must not be empty")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone format")
    public String phone;

    @NotBlank(message = "Password must not be empty")
    public String password;

    /** Device ID for session tracking. */
    public String deviceId;
}
