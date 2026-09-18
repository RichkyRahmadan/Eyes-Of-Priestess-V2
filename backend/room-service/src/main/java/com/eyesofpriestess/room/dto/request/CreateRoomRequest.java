package com.eyesofpriestess.room.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public class CreateRoomRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title cannot exceed 200 characters")
    public String title;

    public String description;

    @JsonAlias({"myRole", "role"})
    @Pattern(regexp = "(?i)^(BUYER|SELLER)$", message = "Role must be BUYER or SELLER")
    public String initiatorRole = "BUYER";

    @JsonAlias({"itemPrice", "price", "totalAmount"})
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000.00", message = "Minimum Room amount is IDR 10.000")
    public BigDecimal amount;

    public Integer autoReleaseHours = 48; // Default 48 hours

    public String getInitiatorRoleNormalized() {
        return initiatorRole != null ? initiatorRole.toUpperCase() : "BUYER";
    }
}

