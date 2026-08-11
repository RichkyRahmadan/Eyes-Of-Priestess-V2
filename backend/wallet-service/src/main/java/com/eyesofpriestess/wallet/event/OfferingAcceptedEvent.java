package com.eyesofpriestess.wallet.event;

import java.math.BigDecimal;

public class OfferingAcceptedEvent {
    public String offeringId;
    public String pilgrimId;
    public BigDecimal amount;
    public String timestamp;

    public OfferingAcceptedEvent() {}

    public OfferingAcceptedEvent(String offeringId, String pilgrimId, BigDecimal amount) {
        this.offeringId = offeringId;
        this.pilgrimId = pilgrimId;
        this.amount = amount;
        this.timestamp = java.time.Instant.now().toString();
    }
}
