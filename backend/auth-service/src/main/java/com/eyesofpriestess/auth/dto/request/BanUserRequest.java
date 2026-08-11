package com.eyesofpriestess.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/** BanUserRequest — Payload for POST /seal/oracle/sanction (Oracle: Insta-Ban). */
public class BanUserRequest {

    @NotNull(message = "User ID must not be empty")
    public UUID userId;

    @NotBlank(message = "Sanction reason must not be empty")
    public String reason; // FRAUDULENT_ACTIVITY | SPAM | HARASSMENT | TERMS_VIOLATION | OTHER

    public String description;

    @NotBlank(message = "Sanction duration must not be empty")
    public String sanctionDuration; // ETERNAL | TEMPORARY

    /** Duration in days if TEMPORARY. */
    public Integer durationDays;
}
