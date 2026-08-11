package com.eyesofpriestess.wallet.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** WithdrawalRequest — Request payload for bank disbursement via Xendit. */
public class WithdrawalRequest {

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "25000.00", message = "Minimum withdrawal is IDR 25.000")
    public BigDecimal amount;

    @NotBlank(message = "Bank code is required")
    public String bankCode; // e.g. BCA, BNI, BRI, MANDIRI

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "^[0-9]{8,20}$", message = "Account number must be 8-20 digits")
    public String accountNumber;

    @NotBlank(message = "Account holder name is required")
    public String accountName;

    @NotBlank(message = "PIN is required to seal the withdrawal")
    @Pattern(regexp = "^[0-9]{6}$", message = "PIN must be 6 digits")
    public String pin;

    public String covenantKey; // Idempotency key
}
