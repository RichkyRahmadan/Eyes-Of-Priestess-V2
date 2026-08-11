package com.eyesofpriestess.room.event;

public class RoomBrokenEvent {
    public String roomId;
    public String initiatorId;
    public String counterpartyId;
    public String reason;
    public String timestamp;

    public RoomBrokenEvent() {}

    public RoomBrokenEvent(String roomId, String initiatorId, String counterpartyId, String reason) {
        this.roomId = roomId;
        this.initiatorId = initiatorId;
        this.counterpartyId = counterpartyId;
        this.reason = reason;
        this.timestamp = java.time.Instant.now().toString();
    }
}
