package com.eyesofpriestess.seal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * RiteRequest — Payload for POST /seal/rite (Login).
 */
public class RiteRequest {

    @NotBlank(message = "Phone resonance must not be empty")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone format")
    public String phone;

    @NotBlank(message = "Password must not be empty")
    public String password;

    /** Device ID for session tracking. */
    public String deviceId;
}
