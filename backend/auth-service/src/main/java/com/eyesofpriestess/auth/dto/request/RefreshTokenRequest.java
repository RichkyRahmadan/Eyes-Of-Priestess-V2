package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/** RefreshTokenRequest — Payload for POST /seal/renew (Refresh Seal). */
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh seal must not be empty")
    public String refreshSeal;
}
