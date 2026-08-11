package com.eyesofpriestess.wallet.event;

import java.math.BigDecimal;

public class CovenantFulfilledEvent {
    public String covenantId;
    public String buyerId;
    public String sellerId;
    public BigDecimal amount;
    public String timestamp;

    public CovenantFulfilledEvent() {}
}
