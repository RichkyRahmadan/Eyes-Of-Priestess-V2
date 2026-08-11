package com.eyesofpriestess.wallet.dto.response;

import com.eyesofpriestess.wallet.entity.Offering;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class OfferingResponse {
    public UUID id;
    public UUID treasuryId;
    public BigDecimal amount;
    public BigDecimal tithe;
    public String gateway;
    public String gatewayTransactionId;
    public String paymentMethod;
    public Map<String, Object> paymentDetails;
    public String status;
    public Instant expiresAt;
    public Instant acceptedAt;
    public Instant createdAt;
    public String invoiceUrl;

    public static OfferingResponse from(Offering o) {
        OfferingResponse r = new OfferingResponse();
        r.id = o.id;
        r.treasuryId = o.treasuryId;
        r.amount = o.amount;
        r.tithe = o.tithe;
        r.gateway = o.gateway;
        r.gatewayTransactionId = o.gatewayTransactionId;
        r.paymentMethod = o.paymentMethod;
        r.paymentDetails = o.paymentDetails;
        r.status = o.status.name();
        r.expiresAt = o.expiresAt;
        r.acceptedAt = o.acceptedAt;
        r.createdAt = o.createdAt;
        if (o.paymentDetails != null && o.paymentDetails.containsKey("invoice_url")) {
            r.invoiceUrl = (String) o.paymentDetails.get("invoice_url");
        }
        return r;
    }
}
