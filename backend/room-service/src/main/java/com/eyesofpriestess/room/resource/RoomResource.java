package com.eyesofpriestess.room.resource;

import com.eyesofpriestess.room.dto.request.*;
import com.eyesofpriestess.room.dto.response.*;
import com.eyesofpriestess.room.service.RoomService;
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

import java.util.UUID;

/**
 * RoomResource — REST resource for Room Sanctum (Escrow Engine).
 * Base path: /api/v1/rooms
 */
@Path("/api/v1/room")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Room Sanctum", description = "Escrow Room creation, state transitions & dispute endpoints")
public class RoomResource {

    @Inject RoomService RoomService;
    @Inject JsonWebToken jwt;

    private UUID currentPilgrimId() {
        return UUID.fromString(jwt.getSubject());
    }

    // ─── 1. FORGE Room (CREATE ROOM) ──────────────────────────────────────

    @POST
    @Authenticated
    @Operation(summary = "Forge a new Room Escrow Room")
    public Uni<Response> forgeCovenant(@Valid CreateRoomRequest req) {
        return RoomService.forgeCovenant(currentPilgrimId(), req)
                .map(res -> Response.status(Response.Status.CREATED)
                        .entity(ApiResponse.ok(res)).build());
    }

    // ─── 2. GET USER rooms ────────────────────────────────────────────────

    @GET
    @Authenticated
    @Operation(summary = "List current User's rooms")
    public Uni<Response> getCovenants(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return RoomService.getPilgrimCovenants(currentPilgrimId(), page, size)
                .map(list -> Response.ok(ApiResponse.ok(list)).build());
    }

    // ─── 3. GET Room DETAILS ──────────────────────────────────────────────

    @GET
    @Path("/{id}")
    @Authenticated
    @Operation(summary = "Get specific Room room details")
    public Uni<Response> getCovenant(@PathParam("id") UUID id) {
        return RoomService.getCovenant(id)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 4. ACCEPT INVITATION ─────────────────────────────────────────────────

    @POST
    @Path("/accept")
    @Authenticated
    @Operation(summary = "Accept an invitation to join a Room room")
    public Uni<Response> acceptCovenant(@Valid AcceptRoomRequest req) {
        return RoomService.acceptCovenant(currentPilgrimId(), req.invitationCode)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 5. SEAL Room (DEPOSIT ESCROW) ────────────────────────────────────

    @POST
    @Path("/{id}/seal")
    @Authenticated
    @Operation(summary = "Buyer: Deposit funds into Escrow (Seal Room)")
    public Uni<Response> sealCovenant(@PathParam("id") UUID id) {
        return RoomService.sealCovenant(currentPilgrimId(), id)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 6. DELIVER Room ──────────────────────────────────────────────────

    @POST
    @Path("/{id}/deliver")
    @Authenticated
    @Operation(summary = "Seller: Declare delivery of item/service")
    public Uni<Response> deliverCovenant(
            @PathParam("id") UUID id,
            DeliverRoomRequest req) {
        return RoomService.deliverCovenant(currentPilgrimId(), id, req)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 7. FULFILL Room (RELEASE PAYOUT) ─────────────────────────────────

    @POST
    @Path("/{id}/fulfill")
    @Authenticated
    @Operation(summary = "Buyer: Confirm fulfillment & release payout to seller")
    public Uni<Response> fulfillCovenant(@PathParam("id") UUID id) {
        return RoomService.fulfillCovenant(currentPilgrimId(), id)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 8. DISPUTE Room ──────────────────────────────────────────────────

    @POST
    @Path("/{id}/dispute")
    @Authenticated
    @Operation(summary = "Open a dispute for a Room (Send to Judgment Sanctum)")
    public Uni<Response> disputeCovenant(
            @PathParam("id") UUID id,
            @Valid DisputeRoomRequest req) {
        return RoomService.disputeCovenant(currentPilgrimId(), id, req)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 9. CANCEL Room ───────────────────────────────────────────────────

    @POST
    @Path("/{id}/cancel")
    @Authenticated
    @Operation(summary = "Initiator: Cancel unsealed Room room")
    public Uni<Response> cancelCovenant(@PathParam("id") UUID id) {
        return RoomService.cancelCovenant(currentPilgrimId(), id)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }
}
