package com.eyesofpriestess.seal.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** OmenVerifyRequest — Payload for POST /seal/omen/verify (Verify OTP). */
public class OmenVerifyRequest {

    @NotBlank(message = "Omen token must not be empty")
    public String omenToken;

    @NotBlank(message = "Omen code must not be empty")
    @Pattern(regexp = "^[0-9]{6}$", message = "Omen code must be 6 digits")
    public String omenCode;
}
