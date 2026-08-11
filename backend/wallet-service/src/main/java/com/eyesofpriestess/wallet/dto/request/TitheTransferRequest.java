package com.eyesofpriestess.wallet.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** TitheTransferRequest — Request payload for peer-to-peer balance transfer. */
public class TitheTransferRequest {

    @NotBlank(message = "Recipient identifier (phone or ID) is required")
    public String recipientIdentifier;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1000.00", message = "Minimum transfer is IDR 1.000")
    public BigDecimal amount;

    public String description;

    @NotBlank(message = "PIN is required")
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN must be 6 digits")
    public String pin;

    public String covenantKey; // Idempotency key
}
