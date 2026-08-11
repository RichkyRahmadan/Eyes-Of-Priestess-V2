package com.eyesofpriestess.dispute.event;

import java.math.BigDecimal;

public class DisputeResolvedEvent {
    public String caseId;
    public String roomId;
    public String buyerId;
    public String sellerId;
    public String resolutionType;   // RELEASE_TO_COUNTERPART | REFUND_TO_INITIATOR | SPLIT
    public BigDecimal amount;
    public Integer splitPercentage; // only for SPLIT
    public String timestamp;

    public DisputeResolvedEvent() {}

    public DisputeResolvedEvent(String caseId, String roomId, String buyerId, String sellerId,
                                  String resolutionType, BigDecimal amount, Integer splitPercentage) {
        this.caseId = caseId;
        this.roomId = roomId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.resolutionType = resolutionType;
        this.amount = amount;
        this.splitPercentage = splitPercentage;
        this.timestamp = java.time.Instant.now().toString();
    }
}
