package com.eyesofpriestess.wallet.event;

import java.math.BigDecimal;

public class DisputeResolvedEvent {
    public String disputeId;
    public String roomId;
    public String buyerId;
    public String sellerId;
    public String winnerRole; // BUYER | SELLER | SPLIT
    public BigDecimal buyerRefundAmount;
    public BigDecimal sellerPayoutAmount;
    public String timestamp;

    public DisputeResolvedEvent() {}
}
