package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * RegisterRequest — Payload for POST /seal/forge (User Registration).
 */
public class RegisterRequest {

    @NotBlank(message = "Phone resonance must not be empty")
    @Pattern(
        regexp = "^\\+?[0-9]{10,15}$",
        message = "Phone must be in valid format (e.g. +628123456789)"
    )
    public String phone;

    public String email;

    @NotBlank(message = "Full name must not be empty")
    @Size(min = 2, max = 100, message = "Full name must be 2-100 characters")
    public String fullName;

    @NotBlank(message = "Password must not be empty")
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(
        regexp = "^(?=.*[a-zA-Z])(?=.*[0-9]).{8,}$",
        message = "Password must contain at least one letter and one number"
    )
    public String password;

    @NotBlank(message = "PIN must not be empty")
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN must be exactly 6 numeric digits")
    public String pin;

    /** OTP verification token from /seal/omen/verify. */
    public String verificationToken;
}
