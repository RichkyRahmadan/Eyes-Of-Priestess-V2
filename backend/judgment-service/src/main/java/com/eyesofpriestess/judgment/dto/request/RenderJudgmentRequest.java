package com.eyesofpriestess.judgment.dto.request;

import com.eyesofpriestess.judgment.entity.JudgmentCase;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RenderJudgmentRequest {

    @NotNull(message = "Resolution type is required")
    public JudgmentCase.ResolutionType resolutionType;

    public String oracleNotes;

    /** Required only if resolutionType == SPLIT. Value: 0-100 (% to counterparty). */
    @Min(value = 1, message = "Split percentage must be at least 1")
    @Max(value = 99, message = "Split percentage must be at most 99")
    public Integer splitPercentage;
}
