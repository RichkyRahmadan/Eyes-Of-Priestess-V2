package com.eyesofpriestess.room.resource;

import com.eyesofpriestess.room.dto.request.*;
import com.eyesofpriestess.room.dto.response.*;
import com.eyesofpriestess.room.service.CovenantService;
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
 * CovenantResource — REST resource for Covenant Sanctum (Escrow Engine).
 * Base path: /api/v1/covenants
 */
@Path("/api/v1/room")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Covenant Sanctum", description = "Escrow Room creation, state transitions & dispute endpoints")
public class CovenantResource {

    @Inject CovenantService covenantService;
    @Inject JsonWebToken jwt;

    private UUID currentPilgrimId() {
        return UUID.fromString(jwt.getSubject());
    }

    // ─── 1. FORGE COVENANT (CREATE ROOM) ──────────────────────────────────────

    @POST
    @Authenticated
    @Operation(summary = "Forge a new Covenant Escrow Room")
    public Uni<Response> forgeCovenant(@Valid ForgeCovenantRequest req) {
        return covenantService.forgeCovenant(currentPilgrimId(), req)
                .map(res -> Response.status(Response.Status.CREATED)
                        .entity(ApiResponse.ok(res)).build());
    }

    // ─── 2. GET USER COVENANTS ────────────────────────────────────────────────

    @GET
    @Authenticated
    @Operation(summary = "List current Pilgrim's covenants")
    public Uni<Response> getCovenants(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return covenantService.getPilgrimCovenants(currentPilgrimId(), page, size)
                .map(list -> Response.ok(ApiResponse.ok(list)).build());
    }

    // ─── 3. GET COVENANT DETAILS ──────────────────────────────────────────────

    @GET
    @Path("/{id}")
    @Authenticated
    @Operation(summary = "Get specific Covenant room details")
    public Uni<Response> getCovenant(@PathParam("id") UUID id) {
        return covenantService.getCovenant(id)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 4. ACCEPT INVITATION ─────────────────────────────────────────────────

    @POST
    @Path("/accept")
    @Authenticated
    @Operation(summary = "Accept an invitation to join a Covenant room")
    public Uni<Response> acceptCovenant(@Valid AcceptCovenantRequest req) {
        return covenantService.acceptCovenant(currentPilgrimId(), req.invitationCode)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 5. SEAL COVENANT (DEPOSIT ESCROW) ────────────────────────────────────

    @POST
    @Path("/{id}/seal")
    @Authenticated
    @Operation(summary = "Buyer: Deposit funds into Escrow (Seal Covenant)")
    public Uni<Response> sealCovenant(@PathParam("id") UUID id) {
        return covenantService.sealCovenant(currentPilgrimId(), id)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 6. DELIVER COVENANT ──────────────────────────────────────────────────

    @POST
    @Path("/{id}/deliver")
    @Authenticated
    @Operation(summary = "Seller: Declare delivery of item/service")
    public Uni<Response> deliverCovenant(
            @PathParam("id") UUID id,
            DeliverCovenantRequest req) {
        return covenantService.deliverCovenant(currentPilgrimId(), id, req)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 7. FULFILL COVENANT (RELEASE PAYOUT) ─────────────────────────────────

    @POST
    @Path("/{id}/fulfill")
    @Authenticated
    @Operation(summary = "Buyer: Confirm fulfillment & release payout to seller")
    public Uni<Response> fulfillCovenant(@PathParam("id") UUID id) {
        return covenantService.fulfillCovenant(currentPilgrimId(), id)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 8. DISPUTE COVENANT ──────────────────────────────────────────────────

    @POST
    @Path("/{id}/dispute")
    @Authenticated
    @Operation(summary = "Open a dispute for a Covenant (Send to Judgment Sanctum)")
    public Uni<Response> disputeCovenant(
            @PathParam("id") UUID id,
            @Valid DisputeCovenantRequest req) {
        return covenantService.disputeCovenant(currentPilgrimId(), id, req)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }

    // ─── 9. CANCEL COVENANT ───────────────────────────────────────────────────

    @POST
    @Path("/{id}/cancel")
    @Authenticated
    @Operation(summary = "Initiator: Cancel unsealed Covenant room")
    public Uni<Response> cancelCovenant(@PathParam("id") UUID id) {
        return covenantService.cancelCovenant(currentPilgrimId(), id)
                .map(res -> Response.ok(ApiResponse.ok(res)).build());
    }
}
