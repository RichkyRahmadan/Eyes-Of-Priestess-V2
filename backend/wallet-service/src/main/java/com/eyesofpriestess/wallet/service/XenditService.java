package com.eyesofpriestess.wallet.service;

import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.UUID;

/**
 * XenditService — Integration service for Xendit Payment Gateway (Sandbox & Production).
 *
 * Supported features:
 * - Create Invoice (VA, QRIS, E-Wallet, Retail Outlet) for TopUpOrder top-ups
 * - Create Disbursement for bank account withdrawals
 * - Webhook callback token validation
 */
@ApplicationScoped
public class XenditService {

    private static final Logger LOG = Logger.getLogger(XenditService.class);

    @ConfigProperty(name = "xendit.api.key", defaultValue = "xnd_development_dummy")
    String apiKey;

    @ConfigProperty(name = "xendit.webhook.token", defaultValue = "xnd_webhook_dummy")
    String webhookToken;

    @ConfigProperty(name = "xendit.callback.url", defaultValue = "http://localhost:8080")
    String callbackUrl;

    @Inject
    Vertx vertx;

    private WebClient webClient;
    private String authHeader;

    @PostConstruct
    void init() {
        this.webClient = WebClient.create(vertx, new WebClientOptions()
                .setDefaultHost("api.xendit.co")
                .setDefaultPort(443)
                .setSsl(true)
                .setConnectTimeout(10000)
        );
        // Xendit uses Basic auth with API key as username and empty password
        String credentials = apiKey + ":";
        this.authHeader = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    /**
     * Create a Xendit Invoice for an TopUpOrder top-up.
     * Endpoint: POST https://api.xendit.co/v2/invoices
     */
    public Uni<JsonObject> createInvoice(UUID offeringId, BigDecimal amount, String pilgrimPhone, String paymentMethod) {
        String externalId = "TopUpOrder-" + offeringId;

        JsonObject body = new JsonObject()
                .put("external_id", externalId)
                .put("amount", amount.longValue())
                .put("description", "Sacred TopUpOrder — EyesOfPriestess Top-Up")
                .put("invoice_duration", 86400) // 24 hours
                .put("success_redirect_url", callbackUrl + "/payment/success")
                .put("failure_redirect_url", callbackUrl + "/payment/failed")
                .put("currency", "IDR");

        if (pilgrimPhone != null) {
            body.put("customer", new JsonObject().put("mobile_number", pilgrimPhone));
        }

        LOG.infof("[XENDIT] Creating Invoice: external_id=%s, amount=%s", externalId, amount);

        if (apiKey == null || apiKey.contains("dummy") || apiKey.contains("development")) {
            LOG.infof("[XENDIT MOCK] Mock invoice generated for dev/test: %s", externalId);
            return Uni.createFrom().item(new JsonObject()
                    .put("id", "inv_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16))
                    .put("external_id", externalId)
                    .put("invoice_url", "https://checkout-staging.xendit.co/v2/" + UUID.randomUUID())
                    .put("expiry_date", java.time.Instant.now().plusSeconds(86400).toString())
                    .put("status", "PENDING"));
        }

        return webClient.post("/v2/invoices")
                .putHeader("Authorization", authHeader)
                .putHeader("Content-Type", "application/json")
                .sendJsonObject(body)
                .map(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        LOG.infof("[XENDIT] Invoice created successfully: %s", response.bodyAsJsonObject().getString("id"));
                        return response.bodyAsJsonObject();
                    } else {
                        LOG.errorf("[XENDIT] Failed to create invoice: status=%d, body=%s",
                                response.statusCode(), response.bodyAsString());
                        throw new RuntimeException("Xendit Invoice creation failed: " + response.bodyAsString());
                    }
                });
    }

    /**
     * Create a Xendit Disbursement for bank account withdrawal.
     * Endpoint: POST https://api.xendit.co/disbursements
     */
    public Uni<JsonObject> createDisbursement(UUID withdrawalId, String bankCode, String accountNumber,
                                              String accountName, BigDecimal amount) {
        String externalId = "WITHDRAWAL-" + withdrawalId;

        JsonObject body = new JsonObject()
                .put("external_id", externalId)
                .put("bank_code", bankCode.toUpperCase())
                .put("account_holder_name", accountName)
                .put("account_number", accountNumber)
                .put("description", "Sacred Wallet Withdrawal — EyesOfPriestess")
                .put("amount", amount.longValue());

        LOG.infof("[XENDIT] Creating Disbursement: external_id=%s, bank=%s, amount=%s",
                externalId, bankCode, amount);

        return webClient.post("/disbursements")
                .putHeader("Authorization", authHeader)
                .putHeader("Content-Type", "application/json")
                .putHeader("X-IDEMPOTENCY-KEY", externalId)
                .sendJsonObject(body)
                .map(response -> {
                    if (response.statusCode() >= 200 && response.statusCode() < 300) {
                        LOG.infof("[XENDIT] Disbursement requested: %s", response.bodyAsJsonObject().getString("id"));
                        return response.bodyAsJsonObject();
                    } else {
                        LOG.errorf("[XENDIT] Failed to create disbursement: status=%d, body=%s",
                                response.statusCode(), response.bodyAsString());
                        throw new RuntimeException("Xendit Disbursement failed: " + response.bodyAsString());
                    }
                });
    }

    /**
     * Verifies the Xendit callback token header against configured webhook token.
     */
    public boolean verifyWebhookToken(String headerToken) {
        if (headerToken == null || webhookToken == null) return false;
        // In dev mode, allow dummy or matching token
        if ("xnd_webhook_dummy".equals(webhookToken)) return true;
        return webhookToken.equals(headerToken);
    }
}
