package com.eyesofpriestess.wallet.event;

import java.math.BigDecimal;

public class TopUpPaidEvent {
    public String offeringId;
    public String userId;
    public BigDecimal amount;
    public String timestamp;

    public TopUpPaidEvent() {}

    public TopUpPaidEvent(String offeringId, String userId, BigDecimal amount) {
        this.offeringId = offeringId;
        this.userId = userId;
        this.amount = amount;
        this.timestamp = java.time.Instant.now().toString();
    }
}
