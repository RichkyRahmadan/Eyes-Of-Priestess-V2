package com.eyesofpriestess.room.dto.request;

import jakarta.validation.constraints.NotBlank;

public class DisputeCovenantRequest {

    @NotBlank(message = "Dispute reason is required")
    public String reason;

    public String description;
    public String evidenceUrl;
}
