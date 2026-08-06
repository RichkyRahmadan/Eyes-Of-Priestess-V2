package com.eyesofpriestess.seal.dto.request;

import jakarta.validation.constraints.NotBlank;

/** SeverRequest — Payload for POST /seal/sever (Logout). */
public class SeverRequest {
    @NotBlank(message = "Refresh seal must not be empty")
    public String refreshSeal;
}
