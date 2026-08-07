package com.eyesofpriestess.communion.resource;

import com.eyesofpriestess.communion.dto.CommunionMessage;
import com.eyesofpriestess.communion.service.CommunionService;
import com.eyesofpriestess.communion.service.RoomRegistry;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * CommunionResource — REST resource for Communion Sanctum.
 * Handles message history retrieval and room status queries.
 * Base path: /api/v1/communion
 */
@Path("/api/v1/communion")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Communion Sanctum", description = "Real-time WebSocket chat endpoints & message history")
public class CommunionResource {

    @Inject CommunionService communionService;
    @Inject RoomRegistry roomRegistry;
    @Inject JsonWebToken jwt;

    /**
     * GET /api/v1/communion/{covenantId}/messages
     * Retrieve persisted message history for a covenant room (paginated, newest first).
     */
    @GET
    @Path("/{covenantId}/messages")
    @Authenticated
    @Operation(summary = "Get message history for a Covenant room")
    public Uni<Response> getHistory(
            @PathParam("covenantId") UUID covenantId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {

        return communionService.getHistory(covenantId, page, size)
                .map(messages -> Response.ok(Map.of(
                        "success", true,
                        "data", messages,
                        "page", page,
                        "size", size,
                        "count", messages.size()
                )).build());
    }

    /**
     * GET /api/v1/communion/{covenantId}/status
     * Returns how many Pilgrims are currently connected to a room.
     */
    @GET
    @Path("/{covenantId}/status")
    @Operation(summary = "Get active session count for a Covenant room")
    public Response getRoomStatus(@PathParam("covenantId") String covenantId) {
        int count = roomRegistry.getActiveSessionCount(covenantId);
        return Response.ok(Map.of(
                "success", true,
                "data", Map.of("covenantId", covenantId, "activeConnections", count)
        )).build();
    }
}
