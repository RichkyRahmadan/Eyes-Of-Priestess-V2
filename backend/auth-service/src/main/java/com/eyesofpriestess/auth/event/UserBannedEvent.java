package com.eyesofpriestess.auth.event;

/**
 * UserBannedEvent — RabbitMQ message published when a User is sanctioned.
 * Consumed by all Sanctums to invalidate caches and reject ongoing requests.
 */
public class UserBannedEvent {

    public String userId;
    public String reason;
    public String timestamp;

    public UserBannedEvent() {}

    public UserBannedEvent(String userId, String reason) {
        this.userId = userId;
        this.reason = reason;
        this.timestamp = java.time.Instant.now().toString();
    }
}
