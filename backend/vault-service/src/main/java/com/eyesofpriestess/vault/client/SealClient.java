package com.eyesofpriestess.vault.client;

import com.eyesofpriestess.vault.dto.response.ApiResponse;
import io.smallrye.mutiny.Uni;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import java.util.Map;
import java.util.UUID;

/** SealClient — REST Client for communication with Seal Sanctum. */
@RegisterRestClient(configKey = "seal-service")
@Path("/api/v1/seal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface SealClient {

    @POST
    @Path("/internal/verify-pin")
    Uni<ApiResponse<Map<String, Boolean>>> verifyPin(
            @QueryParam("pilgrimId") UUID pilgrimId,
            @QueryParam("pin") String pin
    );

    @GET
    @Path("/self")
    Uni<ApiResponse<Map<String, Object>>> getPilgrimSelf(
            @HeaderParam("Authorization") String token
    );
}
