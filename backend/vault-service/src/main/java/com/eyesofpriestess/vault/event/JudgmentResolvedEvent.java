package com.eyesofpriestess.vault.event;

import java.math.BigDecimal;

public class JudgmentResolvedEvent {
    public String disputeId;
    public String covenantId;
    public String buyerId;
    public String sellerId;
    public String winnerRole; // BUYER | SELLER | SPLIT
    public BigDecimal buyerRefundAmount;
    public BigDecimal sellerPayoutAmount;
    public String timestamp;

    public JudgmentResolvedEvent() {}
}
