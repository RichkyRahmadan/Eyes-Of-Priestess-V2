package com.eyesofpriestess.wallet.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/** CreateTopUpRequest — Payload for POST /vault/TopUpOrder (Top-up via Xendit). */
public class CreateTopUpRequest {

    @NotNull(message = "Amount must not be empty")
    @DecimalMin(value = "10000.00", message = "Minimum TopUpOrder is IDR 10.000")
    @DecimalMax(value = "50000000.00", message = "Maximum TopUpOrder is IDR 50.000.000")
    public BigDecimal amount;

    @NotBlank(message = "Payment method must not be empty")
    public String method; // VIRTUAL_ACCOUNT | E_WALLET | QRIS

    // For VIRTUAL_ACCOUNT
    public String bank; // BCA | BNI | BRI | MANDIRI | BSI | PERMATA

    // For E_WALLET
    public String eWalletType; // OVO | DANA | SHOPEEPAY | LINKAJA

    /** Idempotency key — client-generated UUID to prevent duplicate topUpOrders. */
    public String covenantKey;
}
