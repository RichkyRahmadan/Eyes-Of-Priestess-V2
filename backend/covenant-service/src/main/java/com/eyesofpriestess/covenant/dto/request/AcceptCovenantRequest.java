package com.eyesofpriestess.covenant.dto.request;

import jakarta.validation.constraints.NotBlank;

public class AcceptCovenantRequest {

    @NotBlank(message = "Invitation code is required")
    public String invitationCode;
}
