package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/** RenewRequest — Payload for POST /seal/renew (Refresh Seal). */
public class RenewRequest {
    @NotBlank(message = "Refresh seal must not be empty")
    public String refreshSeal;
}
