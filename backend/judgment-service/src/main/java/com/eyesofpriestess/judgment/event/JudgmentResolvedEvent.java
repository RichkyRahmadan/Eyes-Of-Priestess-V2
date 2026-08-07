package com.eyesofpriestess.judgment.event;

import java.math.BigDecimal;

public class JudgmentResolvedEvent {
    public String caseId;
    public String covenantId;
    public String buyerId;
    public String sellerId;
    public String resolutionType;   // RELEASE_TO_COUNTERPART | REFUND_TO_INITIATOR | SPLIT
    public BigDecimal amount;
    public Integer splitPercentage; // only for SPLIT
    public String timestamp;

    public JudgmentResolvedEvent() {}

    public JudgmentResolvedEvent(String caseId, String covenantId, String buyerId, String sellerId,
                                  String resolutionType, BigDecimal amount, Integer splitPercentage) {
        this.caseId = caseId;
        this.covenantId = covenantId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.resolutionType = resolutionType;
        this.amount = amount;
        this.splitPercentage = splitPercentage;
        this.timestamp = java.time.Instant.now().toString();
    }
}
