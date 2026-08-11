package com.eyesofpriestess.auth.resource;

import com.eyesofpriestess.auth.dto.request.*;
import com.eyesofpriestess.auth.dto.response.ApiResponse;
import com.eyesofpriestess.auth.service.OtpService;
import com.eyesofpriestess.auth.service.AuthService;
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

import java.util.Map;
import java.util.UUID;

/**
 * AuthResource — REST resource for the Seal Sanctum.
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
 * GET    /seal/self                 - Get Current User (Gaze Upon Self)
 * PUT    /seal/self                 - Update Profile
 * PUT    /seal/self/attune          - KYC Attunement (TODO: Phase 2)
 * POST   /seal/oracle/sanction      - Sanction User [Oracle only]
 */
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Seal Sanctum", description = "Authentication, Identity & Sanction endpoints")
public class AuthResource {

    @Inject AuthService AuthService;
    @Inject OtpService OtpService;
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
    @Operation(summary = "Forge a new User identity (Register)")
    public Uni<Response> forgeIdentity(@Valid RegisterRequest req) {
        return AuthService.forgeIdentity(req)
                .map(data -> Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.ok(data))
                    .build());
    }

    // ─── 1.2 RITE OF RETURN (LOGIN) ───────────────────────────────────────────

    @POST
    @Path("/rite")
    @Operation(summary = "Perform the Rite of Return (Login)")
    public Uni<Response> rite(@Valid LoginRequest req) {
        return AuthService.rite(req)
                .map(sealResp -> Response.ok(ApiResponse.ok(sealResp)).build());
    }

    // ─── 1.3 RENEW SEAL (REFRESH TOKEN) ──────────────────────────────────────

    @POST
    @Path("/renew")
    @Operation(summary = "Renew Sacred Seal (Refresh Token)")
    public Uni<Response> renew(@Valid RefreshTokenRequest req) {
        return AuthService.renew(req)
                .map(sealResp -> Response.ok(ApiResponse.ok(sealResp)).build());
    }

    // ─── 1.4 SEVER SEAL (LOGOUT) ─────────────────────────────────────────────

    @POST
    @Path("/sever")
    @Authenticated
    @Operation(summary = "Sever the Sacred Seal (Logout)")
    public Uni<Response> severSeal(@Valid LogoutRequest req) {
        String accessJti = currentJti();
        return AuthService.severSeal(accessJti, req)
                .map(v -> Response.ok(ApiResponse.ok(Map.of(
                    "message", "Your seal has been severed. Return safely, User."
                ))).build());
    }

    // ─── 1.5 REQUEST OMEN (OTP) ───────────────────────────────────────────────

    @POST
    @Path("/omen/request")
    @Operation(summary = "Request an Omen (OTP) for phone verification")
    public Uni<Response> requestOmen(@Valid OtpRequest req) {
        return OtpService.requestOmen(req.phone, req.purpose)
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
    public Uni<Response> verifyOmen(@Valid OtpVerifyRequest req) {
        return OtpService.verifyOmen(req.omenToken, req.omenCode)
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
    public Uni<Response> transmutePin(@Valid SetPinRequest req) {
        return AuthService.transmutePin(currentPilgrimId(), req)
                .map(v -> Response.ok(ApiResponse.ok(Map.of(
                    "message", "Your seal has been transmuted successfully"
                ))).build());
    }

    // ─── 1.8 GAZE UPON SELF (GET PROFILE) ────────────────────────────────────

    @GET
    @Path("/self")
    @Authenticated
    @Operation(summary = "Gaze Upon Self — get current User profile")
    public Uni<Response> getSelf() {
        return AuthService.getSelf(currentPilgrimId())
                .map(user -> Response.ok(ApiResponse.ok(user)).build());
    }

    // ─── 1.9 UPDATE SELF (UPDATE PROFILE) ────────────────────────────────────

    @PUT
    @Path("/self")
    @Authenticated
    @Operation(summary = "Update current User profile")
    public Uni<Response> updateSelf(@Valid UpdateUserRequest req) {
        return AuthService.updateSelf(currentPilgrimId(), req)
                .map(user -> Response.ok(ApiResponse.ok(user)).build());
    }

    // ─── 1.10 ORACLE: SANCTION User ───────────────────────────────────────

    @POST
    @Path("/oracle/sanction")
    @RolesAllowed("Oracle")
    @Operation(summary = "Oracle: Sanction a User (Insta-Ban)")
    public Uni<Response> sanctionPilgrim(@Valid BanUserRequest req) {
        UUID oracleId = currentPilgrimId();
        return AuthService.sanctionPilgrim(oracleId, req)
                .map(data -> Response.ok(ApiResponse.ok(data)).build());
    }

    // ─── INTERNAL: PIN VERIFY (for other Sanctums) ────────────────────────────

    @POST
    @Path("/internal/verify-pin")
    @RolesAllowed("Oracle") // Protected internal endpoint
    @Operation(summary = "Internal: Verify a User's PIN (used by other Sanctums)")
    public Uni<Response> verifyPin(
            @QueryParam("userId") UUID userId,
            @QueryParam("pin") String pin) {
        return AuthService.verifyPin(userId, pin)
                .map(valid -> Response.ok(ApiResponse.ok(Map.of("valid", valid))).build());
    }
}
