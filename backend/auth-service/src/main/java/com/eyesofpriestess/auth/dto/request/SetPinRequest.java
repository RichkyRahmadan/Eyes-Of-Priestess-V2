package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** SetPinRequest — Payload for PUT /seal/pin (Change PIN). */
public class SetPinRequest {

    @NotBlank(message = "Old PIN must not be empty")
    @Pattern(regexp = "^[0-9]{6}$", message = "Old PIN must be 6 digits")
    public String oldPin;

    @NotBlank(message = "New PIN must not be empty")
    @Pattern(regexp = "^[0-9]{6}$", message = "New PIN must be 6 digits")
    public String newPin;
}
