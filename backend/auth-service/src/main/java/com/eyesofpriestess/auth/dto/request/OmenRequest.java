package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** OmenRequest — Payload for POST /seal/omen/request (Request OTP). */
public class OmenRequest {

    @NotBlank(message = "Phone resonance must not be empty")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone format")
    public String phone;

    @NotBlank(message = "Omen purpose must not be empty")
    @Pattern(
        regexp = "FORGE|RESET_PASSWORD|CHANGE_PHONE",
        message = "Purpose must be FORGE, RESET_PASSWORD, or CHANGE_PHONE"
    )
    public String purpose;
}
