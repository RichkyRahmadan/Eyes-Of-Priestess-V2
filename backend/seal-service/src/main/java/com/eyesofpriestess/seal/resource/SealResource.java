package com.eyesofpriestess.seal.resource;

import com.eyesofpriestess.seal.dto.request.*;
import com.eyesofpriestess.seal.dto.response.ApiResponse;
import com.eyesofpriestess.seal.service.OmenService;
import com.eyesofpriestess.seal.service.SealService;
import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;
import java.util.UUID;

/**
 * SealResource — REST resource for the Seal Sanctum.
 * Base path: /api/v1/seal
 *
 * Endpoints:
 * POST   /seal/forge                - Register (Forge Identity)
 * POST   /seal/rite                 - Login (Rite of Return)
 * POST   /seal/renew                - Refresh Token (Renew Seal)
 * POST   /seal/sever                - Logout (Sever Seal)
 * POST   /seal/omen/request         - Request OTP
 * POST   /seal/omen/verify          - Verify OTP
 * PUT    /seal/pin                  - Change PIN (Transmute Pin)
 * GET    /seal/self                 - Get Current Pilgrim (Gaze Upon Self)
 * PUT    /seal/self                 - Update Profile
 * PUT    /seal/self/attune          - KYC Attunement (TODO: Phase 2)
 * POST   /seal/oracle/sanction      - Sanction Pilgrim [Oracle only]
 */
@Path("/api/v1/seal")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Seal Sanctum", description = "Authentication, Identity & Sanction endpoints")
public class SealResource {

    @Inject SealService sealService;
    @Inject OmenService omenService;
    @Inject JsonWebToken jwt;

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    private UUID currentPilgrimId() {
        return UUID.fromString(jwt.getSubject());
    }

    private String currentJti() {
        return jwt.getTokenID();
    }

    // ─── 1.1 FORGE IDENTITY (REGISTER) ───────────────────────────────────────

    @POST
    @Path("/forge")
    @Operation(summary = "Forge a new Pilgrim identity (Register)")
    public Uni<Response> forgeIdentity(@Valid ForgeRequest req) {
        return sealService.forgeIdentity(req)
                .map(data -> Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.ok(data))
                    .build());
    }

    // ─── 1.2 RITE OF RETURN (LOGIN) ───────────────────────────────────────────

    @POST
    @Path("/rite")
    @Operation(summary = "Perform the Rite of Return (Login)")
    public Uni<Response> rite(@Valid RiteRequest req) {
        return sealService.rite(req)
                .map(sealResp -> Response.ok(ApiResponse.ok(sealResp)).build());
    }

    // ─── 1.3 RENEW SEAL (REFRESH TOKEN) ──────────────────────────────────────

    @POST
    @Path("/renew")
    @Operation(summary = "Renew Sacred Seal (Refresh Token)")
    public Uni<Response> renew(@Valid RenewRequest req) {
        return sealService.renew(req)
                .map(sealResp -> Response.ok(ApiResponse.ok(sealResp)).build());
    }

    // ─── 1.4 SEVER SEAL (LOGOUT) ─────────────────────────────────────────────

    @POST
    @Path("/sever")
    @Authenticated
    @Operation(summary = "Sever the Sacred Seal (Logout)")
    public Uni<Response> severSeal(@Valid SeverRequest req) {
        String accessJti = currentJti();
        return sealService.severSeal(accessJti, req)
                .map(v -> Response.ok(ApiResponse.ok(Map.of(
                    "message", "Your seal has been severed. Return safely, pilgrim."
                ))).build());
    }

    // ─── 1.5 REQUEST OMEN (OTP) ───────────────────────────────────────────────

    @POST
    @Path("/omen/request")
    @Operation(summary = "Request an Omen (OTP) for phone verification")
    public Uni<Response> requestOmen(@Valid OmenRequest req) {
        return omenService.requestOmen(req.phone, req.purpose)
                .map(omenToken -> Response.ok(ApiResponse.ok(Map.of(
                    "omenToken", omenToken,
                    "expiresIn", 300,
                    "message", "An omen has been sent to your resonance (SIMULATED: 123456)"
                ))).build());
    }

    // ─── 1.6 VERIFY OMEN ─────────────────────────────────────────────────────

    @POST
    @Path("/omen/verify")
    @Operation(summary = "Verify the Omen code")
    public Uni<Response> verifyOmen(@Valid OmenVerifyRequest req) {
        return omenService.verifyOmen(req.omenToken, req.omenCode)
                .map(verificationToken -> {
                    if (verificationToken == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                            .entity(ApiResponse.fail("INVALID_OMEN",
                                "The omen code is incorrect or has expired"))
                            .build();
                    }
                    return Response.ok(ApiResponse.ok(Map.of(
                        "verified", true,
                        "verificationToken", verificationToken
                    ))).build();
                });
    }

    // ─── 1.7 TRANSMUTE PIN (CHANGE PIN) ──────────────────────────────────────

    @PUT
    @Path("/pin")
    @Authenticated
    @Operation(summary = "Transmute the Sacred PIN")
    public Uni<Response> transmutePin(@Valid TransmutePinRequest req) {
        return sealService.transmutePin(currentPilgrimId(), req)
                .map(v -> Response.ok(ApiResponse.ok(Map.of(
                    "message", "Your seal has been transmuted successfully"
                ))).build());
    }

    // ─── 1.8 GAZE UPON SELF (GET PROFILE) ────────────────────────────────────

    @GET
    @Path("/self")
    @Authenticated
    @Operation(summary = "Gaze Upon Self — get current Pilgrim profile")
    public Uni<Response> getSelf() {
        return sealService.getSelf(currentPilgrimId())
                .map(pilgrim -> Response.ok(ApiResponse.ok(pilgrim)).build());
    }

    // ─── 1.9 UPDATE SELF (UPDATE PROFILE) ────────────────────────────────────

    @PUT
    @Path("/self")
    @Authenticated
    @Operation(summary = "Update current Pilgrim profile")
    public Uni<Response> updateSelf(@Valid UpdatePilgrimRequest req) {
        return sealService.updateSelf(currentPilgrimId(), req)
                .map(pilgrim -> Response.ok(ApiResponse.ok(pilgrim)).build());
    }

    // ─── 1.10 ORACLE: SANCTION PILGRIM ───────────────────────────────────────

    @POST
    @Path("/oracle/sanction")
    @RolesAllowed("Oracle")
    @Operation(summary = "Oracle: Sanction a Pilgrim (Insta-Ban)")
    public Uni<Response> sanctionPilgrim(@Valid SanctionRequest req) {
        UUID oracleId = currentPilgrimId();
        return sealService.sanctionPilgrim(oracleId, req)
                .map(data -> Response.ok(ApiResponse.ok(data)).build());
    }

    // ─── INTERNAL: PIN VERIFY (for other Sanctums) ────────────────────────────

    @POST
    @Path("/internal/verify-pin")
    @RolesAllowed("Oracle") // Protected internal endpoint
    @Operation(summary = "Internal: Verify a Pilgrim's PIN (used by other Sanctums)")
    public Uni<Response> verifyPin(
            @QueryParam("pilgrimId") UUID pilgrimId,
            @QueryParam("pin") String pin) {
        return sealService.verifyPin(pilgrimId, pin)
                .map(valid -> Response.ok(ApiResponse.ok(Map.of("valid", valid))).build());
    }
}
