package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/** LogoutRequest — Payload for POST /seal/sever (Logout). */
public class LogoutRequest {
    @NotBlank(message = "Refresh seal must not be empty")
    public String refreshSeal;
}
