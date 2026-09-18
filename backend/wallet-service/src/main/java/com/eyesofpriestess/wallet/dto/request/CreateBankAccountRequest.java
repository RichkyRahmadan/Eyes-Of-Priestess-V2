package com.eyesofpriestess.wallet.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateBankAccountRequest {

    @NotBlank(message = "Bank code is required")
    @Size(max = 20, message = "Bank code must not exceed 20 characters")
    public String bankCode;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    public String bankName;

    @NotBlank(message = "Account number is required")
    @Size(max = 50, message = "Account number must not exceed 50 characters")
    public String accountNumber;

    @NotBlank(message = "Account holder name is required")
    @Size(max = 100, message = "Account holder name must not exceed 100 characters")
    public String accountHolderName;

    @JsonAlias({"primary", "is_primary"})
    public Boolean isPrimary;
}
