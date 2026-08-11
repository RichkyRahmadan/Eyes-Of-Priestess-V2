package com.eyesofpriestess.wallet.client;

import com.eyesofpriestess.wallet.dto.response.ApiResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Map;
import java.util.UUID;

/** AuthClient — REST Client for communication with Seal Sanctum. */
@RegisterRestClient(configKey = "auth-service")
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface AuthClient {

    @POST
    @Path("/internal/verify-pin")
    Uni<ApiResponse<Map<String, Boolean>>> verifyPin(
            @QueryParam("userId") UUID userId,
            @QueryParam("pin") String pin
    );

    @GET
    @Path("/self")
    Uni<ApiResponse<Map<String, Object>>> getPilgrimSelf(
            @HeaderParam("Authorization") String token
    );
}
