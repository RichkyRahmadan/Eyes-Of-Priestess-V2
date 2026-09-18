package com.eyesofpriestess.wallet.resource;

import com.eyesofpriestess.wallet.dto.request.*;
import com.eyesofpriestess.wallet.dto.response.*;
import com.eyesofpriestess.wallet.service.WalletService;
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
 * WalletResource — REST resource for Vault Sanctum (Wallet, Xendit Payments & Escrow).
 * Base path: /api/v1/vault
 */
@Path("/api/v1/wallet")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Vault Sanctum", description = "Wallet, Xendit TopUpOrder/Withdrawal & Escrow endpoints")
public class WalletResource {

    @Inject WalletService WalletService;
    @Inject JsonWebToken jwt;

    private UUID currentPilgrimId() {
        return UUID.fromString(jwt.getSubject());
    }

    // ─── 1. GAZE UPON Wallet (GET BALANCE) ───────────────────────────────────

    @GET
    @Path("/balance")
    @Authenticated
    @Operation(summary = "Get Wallet balance and financial summary")
    public Uni<Response> getBalance() {
        return getTreasury();
    }

    @GET
    @Path("/Wallet")
    @Authenticated
    @Operation(summary = "Get Wallet balance and financial summary (alias)")
    public Uni<Response> getTreasury() {
        return WalletService.getOrCreateTreasury(currentPilgrimId())
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 2. MAKE TopUpOrder (XENDIT TOP-UP) ─────────────────────────────────────

    @POST
    @Path("/topup")
    @Authenticated
    @Operation(summary = "Create topup request via Xendit")
    public Uni<Response> makeTopup(@Valid CreateTopUpRequest req) {
        return makeOffering(req);
    }

    @POST
    @Path("/TopUpOrder")
    @Authenticated
    @Operation(summary = "Create an TopUpOrder top-up request via Xendit (alias)")
    public Uni<Response> makeOffering(@Valid CreateTopUpRequest req) {
        return WalletService.makeOffering(currentPilgrimId(), req)
                .map(res -> Response.status(Response.Status.CREATED)
                        .entity(ApiResponse.ok(res)).build());
    }

    // ─── 3. REQUEST WITHDRAWAL (XENDIT DISBURSEMENT) ──────────────────────────

    @POST
    @Path("/withdraw")
    @Authenticated
    @Operation(summary = "Request bank account withdrawal via Xendit Disbursement")
    public Uni<Response> requestWithdraw(@Valid WithdrawalRequest req) {
        return requestWithdrawal(req);
    }

    @POST
    @Path("/withdrawal")
    @Authenticated
    @Operation(summary = "Request bank account withdrawal via Xendit Disbursement (alias)")
    public Uni<Response> requestWithdrawal(@Valid WithdrawalRequest req) {
        return WalletService.requestWithdrawal(currentPilgrimId(), req)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 3b. BANK ACCOUNTS ────────────────────────────────────────────────────

    @GET
    @Path("/bank-accounts")
    @Authenticated
    @Operation(summary = "List verified linked bank accounts")
    public Uni<Response> getBankAccounts() {
        return WalletService.getBankAccounts(currentPilgrimId())
                .map(list -> Response.ok(ApiResponse.ok(list)).build());
    }

    @POST
    @Path("/bank-accounts")
    @Authenticated
    @Operation(summary = "Link a new bank account")
    public Uni<Response> addBankAccount(@Valid CreateBankAccountRequest req) {
        return WalletService.addBankAccount(currentPilgrimId(), req)
                .map(res -> Response.status(Response.Status.CREATED)
                        .entity(ApiResponse.ok(res)).build());
    }

    @DELETE
    @Path("/bank-accounts/{id}")
    @Authenticated
    @Operation(summary = "Unlink / soft-delete a bank account")
    public Uni<Response> deleteBankAccount(@PathParam("id") UUID id) {
        return WalletService.deleteBankAccount(currentPilgrimId(), id)
                .map(v -> Response.ok(ApiResponse.ok(Map.of("message", "Bank account unlinked"))).build());
    }


    // ─── 4. P2P TITHE TRANSFER ────────────────────────────────────────────────

    @POST
    @Path("/transfer")
    @Authenticated
    @Operation(summary = "Transfer tithe balance to another User")
    public Uni<Response> titheTransfer(
            @QueryParam("recipientId") UUID recipientId,
            @Valid TransferRequest req) {
        return WalletService.tithingTransfer(currentPilgrimId(), recipientId, req)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 5. READ transactions (TRANSACTION HISTORY) ─────────────────────────────

    @GET
    @Path("/transactions")
    @Authenticated
    @Operation(summary = "Get paginated transaction history")
    public Uni<Response> getChronicles(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return WalletService.getChronicles(currentPilgrimId(), page, size)
                .map(list -> Response.ok(ApiResponse.ok(list)).build());
    }

    // ─── 6. XENDIT WEBHOOK RECEIVER ───────────────────────────────────────────

    @POST
    @Path("/webhook/xendit")
    @Operation(summary = "Xendit webhook callback endpoint for payment confirmation")
    public Uni<Response> xenditWebhook(
            @HeaderParam("x-callback-token") String callbackToken,
            XenditWebhookPayload payload) {
        return WalletService.handleXenditWebhook(callbackToken, payload)
                .map(res -> Response.ok(res).build());
    }

    // ─── 7. INTERNAL ESCROW API ───────────────────────────────────────────────

    @POST
    @Path("/internal/escrow/hold")
    @Operation(summary = "Internal: Lock funds into Escrow for a Room")
    public Uni<Response> holdEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("roomId") String roomId) {
        return WalletService.sealEscrowHold(buyerId, amount, roomId)
                .map(v -> Response.ok(ApiResponse.ok(Map.of("status", "held"))).build());
    }

    @POST
    @Path("/internal/escrow/release")
    @Operation(summary = "Internal: Release Escrow funds to seller")
    public Uni<Response> releaseEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("sellerId") UUID sellerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("roomId") String roomId) {
        return WalletService.releaseEscrow(buyerId, sellerId, amount, roomId)
                .map(v -> Response.ok(ApiResponse.ok(Map.of("status", "released"))).build());
    }

    @POST
    @Path("/internal/escrow/refund")
    @Operation(summary = "Internal: Refund Escrow funds to buyer")
    public Uni<Response> refundEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("roomId") String roomId) {
        return WalletService.refundEscrow(buyerId, amount, roomId)
                .map(v -> Response.ok(ApiResponse.ok(Map.of("status", "refunded"))).build());
    }
}
