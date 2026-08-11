package com.eyesofpriestess.wallet.resource;

import com.eyesofpriestess.wallet.dto.request.*;
import com.eyesofpriestess.wallet.dto.response.*;
import com.eyesofpriestess.wallet.service.VaultService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * VaultResource — REST resource for Vault Sanctum (Treasury, Xendit Payments & Escrow).
 * Base path: /api/v1/vault
 */
@Path("/api/v1/wallet")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Vault Sanctum", description = "Treasury, Xendit Offering/Withdrawal & Escrow endpoints")
public class VaultResource {

    @Inject VaultService vaultService;
    @Inject JsonWebToken jwt;

    private UUID currentPilgrimId() {
        return UUID.fromString(jwt.getSubject());
    }

    // ─── 1. GAZE UPON TREASURY (GET BALANCE) ───────────────────────────────────

    @GET
    @Path("/treasury")
    @Authenticated
    @Operation(summary = "Get Treasury balance and financial summary")
    public Uni<Response> getTreasury() {
        return vaultService.getOrCreateTreasury(currentPilgrimId())
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 2. MAKE OFFERING (XENDIT TOP-UP) ─────────────────────────────────────

    @POST
    @Path("/offering")
    @Authenticated
    @Operation(summary = "Create an Offering top-up request via Xendit")
    public Uni<Response> makeOffering(@Valid MakeOfferingRequest req) {
        return vaultService.makeOffering(currentPilgrimId(), req)
                .map(res -> Response.status(Response.Status.CREATED)
                        .entity(ApiResponse.ok(res)).build());
    }

    // ─── 3. REQUEST WITHDRAWAL (XENDIT DISBURSEMENT) ──────────────────────────

    @POST
    @Path("/withdrawal")
    @Authenticated
    @Operation(summary = "Request bank account withdrawal via Xendit Disbursement")
    public Uni<Response> requestWithdrawal(@Valid WithdrawalRequest req) {
        return vaultService.requestWithdrawal(currentPilgrimId(), req)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 4. P2P TITHE TRANSFER ────────────────────────────────────────────────

    @POST
    @Path("/transfer")
    @Authenticated
    @Operation(summary = "Transfer tithe balance to another Pilgrim")
    public Uni<Response> titheTransfer(
            @QueryParam("recipientId") UUID recipientId,
            @Valid TitheTransferRequest req) {
        return vaultService.tithingTransfer(currentPilgrimId(), recipientId, req)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 5. READ CHRONICLES (TRANSACTION HISTORY) ─────────────────────────────

    @GET
    @Path("/chronicles")
    @Authenticated
    @Operation(summary = "Get paginated transaction history")
    public Uni<Response> getChronicles(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return vaultService.getChronicles(currentPilgrimId(), page, size)
                .map(list -> Response.ok(ApiResponse.ok(list)).build());
    }

    // ─── 6. XENDIT WEBHOOK RECEIVER ───────────────────────────────────────────

    @POST
    @Path("/webhook/xendit")
    @Operation(summary = "Xendit webhook callback endpoint for payment confirmation")
    public Uni<Response> xenditWebhook(
            @HeaderParam("x-callback-token") String callbackToken,
            XenditWebhookPayload payload) {
        return vaultService.handleXenditWebhook(callbackToken, payload)
                .map(res -> Response.ok(res).build());
    }

    // ─── 7. INTERNAL ESCROW API ───────────────────────────────────────────────

    @POST
    @Path("/internal/escrow/hold")
    @Operation(summary = "Internal: Lock funds into Escrow for a Covenant")
    public Uni<Response> holdEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("covenantId") String covenantId) {
        return vaultService.sealEscrowHold(buyerId, amount, covenantId)
                .map(v -> Response.ok(ApiResponse.ok(Map.of("status", "held"))).build());
    }

    @POST
    @Path("/internal/escrow/release")
    @Operation(summary = "Internal: Release Escrow funds to seller")
    public Uni<Response> releaseEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("sellerId") UUID sellerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("covenantId") String covenantId) {
        return vaultService.releaseEscrow(buyerId, sellerId, amount, covenantId)
                .map(v -> Response.ok(ApiResponse.ok(Map.of("status", "released"))).build());
    }

    @POST
    @Path("/internal/escrow/refund")
    @Operation(summary = "Internal: Refund Escrow funds to buyer")
    public Uni<Response> refundEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("covenantId") String covenantId) {
        return vaultService.refundEscrow(buyerId, amount, covenantId)
                .map(v -> Response.ok(ApiResponse.ok(Map.of("status", "refunded"))).build());
    }
}
