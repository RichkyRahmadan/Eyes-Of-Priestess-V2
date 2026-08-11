package com.eyesofpriestess.wallet.event;

import java.math.BigDecimal;

public class RoomCompletedEvent {
    public String roomId;
    public String buyerId;
    public String sellerId;
    public BigDecimal amount;
    public String timestamp;

    public RoomCompletedEvent() {}
}
