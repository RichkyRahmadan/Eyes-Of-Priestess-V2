package com.eyesofpriestess.covenant.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class ForgeCovenantRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    public String title;

    public String description;

    @NotBlank(message = "Initiator role is required (BUYER or SELLER)")
    @Pattern(regexp = "^(BUYER|SELLER)$", message = "Role must be BUYER or SELLER")
    public String initiatorRole;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000.00", message = "Minimum Covenant amount is IDR 10.000")
    public BigDecimal amount;

    public Integer autoReleaseHours = 48; // Default 48 hours
}
