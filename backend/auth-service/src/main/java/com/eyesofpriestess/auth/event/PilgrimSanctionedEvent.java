package com.eyesofpriestess.auth.event;

/**
 * PilgrimSanctionedEvent — RabbitMQ message published when a Pilgrim is sanctioned.
 * Consumed by all Sanctums to invalidate caches and reject ongoing requests.
 */
public class PilgrimSanctionedEvent {

    public String pilgrimId;
    public String reason;
    public String timestamp;

    public PilgrimSanctionedEvent() {}

    public PilgrimSanctionedEvent(String pilgrimId, String reason) {
        this.pilgrimId = pilgrimId;
        this.reason = reason;
        this.timestamp = java.time.Instant.now().toString();
    }
}
