package com.eyesofpriestess.covenant.event;

public class CovenantBrokenEvent {
    public String covenantId;
    public String initiatorId;
    public String counterpartyId;
    public String reason;
    public String timestamp;

    public CovenantBrokenEvent() {}

    public CovenantBrokenEvent(String covenantId, String initiatorId, String counterpartyId, String reason) {
        this.covenantId = covenantId;
        this.initiatorId = initiatorId;
        this.counterpartyId = counterpartyId;
        this.reason = reason;
        this.timestamp = java.time.Instant.now().toString();
    }
}
