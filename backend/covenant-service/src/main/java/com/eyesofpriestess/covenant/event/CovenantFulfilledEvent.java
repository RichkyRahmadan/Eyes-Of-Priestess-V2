package com.eyesofpriestess.covenant.event;

import java.math.BigDecimal;

public class CovenantFulfilledEvent {
    public String covenantId;
    public String buyerId;
    public String sellerId;
    public BigDecimal amount;
    public String timestamp;

    public CovenantFulfilledEvent() {}

    public CovenantFulfilledEvent(String covenantId, String buyerId, String sellerId, BigDecimal amount) {
        this.covenantId = covenantId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.timestamp = java.time.Instant.now().toString();
    }
}
