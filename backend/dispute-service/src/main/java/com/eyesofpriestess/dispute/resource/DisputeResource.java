package com.eyesofpriestess.dispute.resource;

import com.eyesofpriestess.dispute.dto.request.ResolveDisputeRequest;
import com.eyesofpriestess.dispute.dto.request.SubmitEvidenceRequest;
import com.eyesofpriestess.dispute.dto.response.ApiResponse;
import com.eyesofpriestess.dispute.service.DisputeService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DisputeResource — REST resource for Judgment Sanctum.
 * Base path: /api/v1/judgment
 *
 * Public endpoints (authenticated): GET cases, submit evidence.
 * Oracle-only endpoints: assign, render.
 */
@Path("/api/v1/dispute")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Judgment Sanctum", description = "Dispute case management and Oracle resolution panel")
public class DisputeResource {

    @Inject DisputeService DisputeService;
    @Inject JsonWebToken jwt;

    private UUID currentPilgrimId() {
        return UUID.fromString(jwt.getSubject());
    }

    // ─── 1. GET MY CASES ──────────────────────────────────────────────────────

    @GET
    @Path("/my-cases")
    @Authenticated
    @Operation(summary = "Get current User's dispute cases")
    public Uni<Response> getMyCases() {
        return DisputeService.getMyCases(currentPilgrimId())
                .map(list -> Response.ok(ApiResponse.ok(list)).build());
    }

    // ─── 2. GET CASE DETAILS ──────────────────────────────────────────────────

    @GET
    @Path("/{id}")
    @Authenticated
    @Operation(summary = "Get specific judgment case details")
    public Uni<Response> getCase(@PathParam("id") UUID id) {
        return DisputeService.getCase(id)
                .map(c -> Response.ok(ApiResponse.ok(c)).build());
    }

    // ─── 3. SUBMIT EVIDENCE ───────────────────────────────────────────────────

    @POST
    @Path("/{id}/evidence")
    @Authenticated
    @Operation(summary = "Submit evidence for a judgment case")
    public Uni<Response> submitEvidence(@PathParam("id") UUID id, @Valid SubmitEvidenceRequest req) {
        return DisputeService.submitEvidence(id, currentPilgrimId(), req)
                .map(c -> Response.ok(ApiResponse.ok(c)).build());
    }

    // ─── 4. ORACLE: LIST OPEN CASES ───────────────────────────────────────────

    @GET
    @Path("/oracle/open")
    @RolesAllowed("oracle")
    @Operation(summary = "Oracle: List all OPEN dispute cases awaiting assignment")
    public Uni<Response> listOpenCases(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return DisputeService.listOpenCases(page, size)
                .map(list -> Response.ok(ApiResponse.ok(list)).build());
    }

    // ─── 5. ORACLE: LIST DELIBERATING CASES ───────────────────────────────────

    @GET
    @Path("/oracle/deliberating")
    @RolesAllowed("oracle")
    @Operation(summary = "Oracle: List all cases currently under deliberation")
    public Uni<Response> listDeliberatingCases(
            @QueryParam("page") @DefaultValue("0") int page,
            @QueryParam("size") @DefaultValue("20") int size) {
        return DisputeService.listDeliberatingCases(page, size)
                .map(list -> Response.ok(ApiResponse.ok(list)).build());
    }

    // ─── 6. ORACLE: ASSIGN CASE ───────────────────────────────────────────────

    @POST
    @Path("/oracle/{id}/assign")
    @RolesAllowed("oracle")
    @Operation(summary = "Oracle: Assign and take a case (moves to DELIBERATING)")
    public Uni<Response> assignCase(@PathParam("id") UUID id) {
        return DisputeService.assignCase(id, currentPilgrimId())
                .map(c -> Response.ok(ApiResponse.ok(c)).build());
    }

    // ─── 7. ORACLE: RENDER JUDGMENT ───────────────────────────────────────────

    @POST
    @Path("/oracle/{id}/render")
    @RolesAllowed("oracle")
    @Operation(summary = "Oracle: Render a judgment verdict and trigger financial settlement")
    public Uni<Response> renderJudgment(
            @PathParam("id") UUID id,
            @Valid ResolveDisputeRequest req,
            @QueryParam("buyerId") UUID buyerId,
            @QueryParam("sellerId") UUID sellerId,
            @QueryParam("amount") BigDecimal amount) {

        return DisputeService.renderJudgment(id, currentPilgrimId(), req, buyerId, sellerId, amount)
                .map(c -> Response.ok(ApiResponse.ok(c)).build());
    }
}
