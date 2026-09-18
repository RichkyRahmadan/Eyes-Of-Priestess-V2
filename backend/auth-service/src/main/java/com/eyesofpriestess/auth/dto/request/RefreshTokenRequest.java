package com.eyesofpriestess.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;

/** RefreshTokenRequest — Payload for POST /seal/renew (Refresh Seal). */
public class RefreshTokenRequest {
    @JsonAlias({"refreshToken", "refresh_token", "token"})
    @NotBlank(message = "Refresh seal must not be empty")
    public String refreshSeal;
}

