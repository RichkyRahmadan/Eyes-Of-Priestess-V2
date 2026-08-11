package com.eyesofpriestess.dispute.client;

import com.eyesofpriestess.dispute.dto.response.ApiResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/**
 * WalletClient — REST Client for Vault Sanctum escrow resolution calls.
 * Invoked after Oracle renders a judgment to execute the financial settlement.
 */
@RegisterRestClient(configKey = "wallet-service")
@Path("/api/v1/vault/internal/escrow")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface WalletClient {

    @POST
    @Path("/release")
    Uni<ApiResponse<Map<String, String>>> releaseEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("sellerId") UUID sellerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("covenantId") String covenantId
    );

    @POST
    @Path("/refund")
    Uni<ApiResponse<Map<String, String>>> refundEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("covenantId") String covenantId
    );

    @POST
    @Path("/split")
    Uni<ApiResponse<Map<String, String>>> splitEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("sellerId") UUID sellerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("covenantId") String covenantId,
            @QueryParam("sellerPct") int sellerPct
    );
}
