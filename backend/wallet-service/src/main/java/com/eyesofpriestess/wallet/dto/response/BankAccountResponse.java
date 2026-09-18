package com.eyesofpriestess.wallet.dto.response;

import com.eyesofpriestess.wallet.entity.BankAccount;

import java.time.Instant;
import java.util.UUID;

public class BankAccountResponse {
    public UUID id;
    public UUID userId;
    public String bankCode;
    public String bankName;
    public String accountNumber;
    public String accountHolderName;
    public boolean isPrimary;
    public boolean isVerified;
    public Instant createdAt;
    public Instant updatedAt;

    public static BankAccountResponse from(BankAccount a) {
        BankAccountResponse res = new BankAccountResponse();
        res.id = a.id;
        res.userId = a.userId;
        res.bankCode = a.bankCode;
        res.bankName = a.bankName;
        res.accountNumber = a.accountNumber;
        res.accountHolderName = a.accountHolderName;
        res.isPrimary = a.isPrimary;
        res.isVerified = a.isVerified;
        res.createdAt = a.createdAt;
        res.updatedAt = a.updatedAt;
        return res;
    }
}
