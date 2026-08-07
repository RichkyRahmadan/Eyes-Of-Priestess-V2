package com.eyesofpriestess.judgment.dto.request;

import jakarta.validation.constraints.NotBlank;

public class SubmitEvidenceRequest {

    @NotBlank(message = "Evidence URL is required")
    public String evidenceUrl;

    public String description;
}
