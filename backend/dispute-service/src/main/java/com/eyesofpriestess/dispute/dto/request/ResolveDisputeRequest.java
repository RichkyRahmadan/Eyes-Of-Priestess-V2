package com.eyesofpriestess.dispute.dto.request;

import com.eyesofpriestess.dispute.entity.DisputeCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ResolveDisputeRequest {

    @NotNull(message = "Resolution type is required")
    public DisputeCase.ResolutionType resolutionType;

    public String oracleNotes;

    /** Required only if resolutionType == SPLIT. Value: 0-100 (% to counterparty). */
    @Min(value = 1, message = "Split percentage must be at least 1")
    @Max(value = 99, message = "Split percentage must be at most 99")
    public Integer splitPercentage;
}
