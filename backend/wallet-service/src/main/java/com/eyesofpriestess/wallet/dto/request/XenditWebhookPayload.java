package com.eyesofpriestess.wallet.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

/** XenditWebhookPayload — Incoming webhook payload from Xendit for Invoice & Disbursement events. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class XenditWebhookPayload {

    public String event;
    public String id;
    
    @JsonProperty("external_id")
    public String externalId;

    public String status;
    public BigDecimal amount;
    
    @JsonProperty("paid_amount")
    public BigDecimal paidAmount;

    @JsonProperty("payment_method")
    public String paymentMethod;

    @JsonProperty("payment_channel")
    public String paymentChannel;

    @JsonProperty("paid_at")
    public String paidAt;

    public Map<String, Object> data;
}
