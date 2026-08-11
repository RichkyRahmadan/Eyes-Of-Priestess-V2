package com.eyesofpriestess.room.event;

import java.math.BigDecimal;

public class RoomCompletedEvent {
    public String roomId;
    public String buyerId;
    public String sellerId;
    public BigDecimal amount;
    public String timestamp;

    public RoomCompletedEvent() {}

    public RoomCompletedEvent(String roomId, String buyerId, String sellerId, BigDecimal amount) {
        this.roomId = roomId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.amount = amount;
        this.timestamp = java.time.Instant.now().toString();
    }
}
