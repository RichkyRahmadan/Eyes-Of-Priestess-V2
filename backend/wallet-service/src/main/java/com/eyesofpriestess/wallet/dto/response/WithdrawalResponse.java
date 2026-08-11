package com.eyesofpriestess.wallet.dto.response;

import com.eyesofpriestess.wallet.entity.Withdrawal;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class WithdrawalResponse {
    public UUID id;
    public UUID treasuryId;
    public BigDecimal amount;
    public BigDecimal tithe;
    public BigDecimal netAmount;
    public String bankCode;
    public String bankName;
    public String accountNumberMasked;
    public String accountName;
    public String status;
    public Instant processedAt;
    public Instant completedAt;
    public Instant createdAt;

    public static WithdrawalResponse from(Withdrawal w) {
        WithdrawalResponse r = new WithdrawalResponse();
        r.id = w.id;
        r.treasuryId = w.treasuryId;
        r.amount = w.amount;
        r.tithe = w.tithe;
        r.netAmount = w.netAmount;
        r.bankCode = w.bankCode;
        r.bankName = w.bankName;
        r.accountNumberMasked = w.accountNumberMasked;
        r.accountName = w.accountName;
        r.status = w.status.name();
        r.processedAt = w.processedAt;
        r.completedAt = w.completedAt;
        r.createdAt = w.createdAt;
        return r;
    }
}
