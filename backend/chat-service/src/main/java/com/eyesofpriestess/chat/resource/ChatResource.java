package com.eyesofpriestess.chat.resource;

import com.eyesofpriestess.chat.dto.ChatMessageDTO;
import com.eyesofpriestess.chat.service.ChatService;
import com.eyesofpriestess.chat.service.RoomRegistry;
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
 * ChatResource — REST resource for Communion Sanctum.
 * Handles message history retrieval and room status queries.
 * Base path: /api/v1/communion
 */
@Path("/api/v1/chat")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Communion Sanctum", description = "Real-time WebSocket chat endpoints & message history")
public class ChatResource {

    @Inject ChatService ChatService;
    @Inject RoomRegistry roomRegistry;
    @Inject JsonWebToken jwt;

    /**
     * GET /api/v1/communion/{roomId}/messages
     * Retrieve persisted message history for a Room room (paginated, newest first).
     */
    @GET
    @Path("/{roomId}/messages")
    @Authenticated
    @Operation(summary = "Get message history for a Room room")
    public Uni<Response> getHistory(
            @PathParam("roomId") UUID roomId,
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("50") int size) {

        return ChatService.getHistory(roomId, page, size)
                .map(messages -> Response.ok(Map.of(
                        "success", true,
                        "data", messages,
                        "page", page,
                        "size", size,
                        "count", messages.size()
                )).build());
    }

    /**
     * GET /api/v1/communion/{roomId}/status
     * Returns how many users are currently connected to a room.
     */
    @GET
    @Path("/{roomId}/status")
    @Operation(summary = "Get active session count for a Room room")
    public Response getRoomStatus(@PathParam("roomId") String roomId) {
        int count = roomRegistry.getActiveSessionCount(roomId);
        return Response.ok(Map.of(
                "success", true,
                "data", Map.of("roomId", roomId, "activeConnections", count)
        )).build();
    }
}
