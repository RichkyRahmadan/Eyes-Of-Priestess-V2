package com.eyesofpriestess.room.client;

import com.eyesofpriestess.room.dto.response.ApiResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

/** WalletClient — REST Client for communication with Vault Sanctum (Escrow ops). */
@RegisterRestClient(configKey = "wallet-service")
@Path("/api/v1/vault/internal/escrow")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface WalletClient {

    @POST
    @Path("/hold")
    Uni<ApiResponse<Map<String, String>>> holdEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("roomId") String roomId
    );

    @POST
    @Path("/release")
    Uni<ApiResponse<Map<String, String>>> releaseEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("sellerId") UUID sellerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("roomId") String roomId
    );

    @POST
    @Path("/refund")
    Uni<ApiResponse<Map<String, String>>> refundEscrow(
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("amount") BigDecimal amount,
            @QueryParam("roomId") String roomId
    );
}
