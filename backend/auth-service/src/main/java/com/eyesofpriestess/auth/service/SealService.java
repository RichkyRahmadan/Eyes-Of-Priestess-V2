package com.eyesofpriestess.auth.service;

import com.eyesofpriestess.auth.dto.request.*;
import com.eyesofpriestess.auth.dto.response.PilgrimResponse;
import com.eyesofpriestess.auth.dto.response.SealResponse;
import com.eyesofpriestess.auth.entity.Pilgrim;
import com.eyesofpriestess.auth.entity.SanctionRecord;
import com.eyesofpriestess.auth.entity.Session;
import com.eyesofpriestess.auth.event.PilgrimSanctionedEvent;
import com.eyesofpriestess.auth.exception.SealException;
import com.eyesofpriestess.auth.repository.PilgrimRepository;
import com.eyesofpriestess.auth.repository.SessionRepository;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * SealService — Core business logic for the Seal Sanctum.
 *
 * Handles:
 * - Forge Identity (Registration)
 * - Rite of Return (Login)
 * - Seal Renewal (Refresh Token)
 * - Sever Seal (Logout)
 * - PIN Sanctification (Change PIN)
 * - Sanction Seal (Insta-Ban)
 * - Self profile read/update
 */
@ApplicationScoped
public class SealService {

    private static final Logger LOG = Logger.getLogger(SealService.class);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_PIN_ATTEMPTS = 5;

    @Inject PilgrimRepository pilgrimRepo;
    @Inject SessionRepository sessionRepo;
    @Inject JwtService jwtService;
    @Inject CrystalService crystalService;
    @Inject OmenService omenService;

    @Inject
    @Channel("pilgrim-sanctioned")
    Emitter<PilgrimSanctionedEvent> sanctionedEmitter;

    // ─── 1. FORGE IDENTITY (REGISTRATION) ─────────────────────────────────────

    @WithTransaction
    public Uni<Map<String, Object>> forgeIdentity(ForgeRequest req) {
        return pilgrimRepo.existsByPhone(req.phone)
                .flatMap(phoneExists -> {
                    if (phoneExists) {
                        throw SealException.conflict(
                            "PHONE_ALREADY_ATTUNED",
                            "This resonance is already attuned to another pilgrim",
                            "phone"
                        );
                    }
                    return req.email != null
                        ? pilgrimRepo.existsByEmail(req.email)
                        : Uni.createFrom().item(false);
                })
                .flatMap(emailExists -> {
                    if (emailExists) {
                        throw SealException.conflict(
                            "EMAIL_ALREADY_ATTUNED",
                            "This email is already bound to another pilgrim",
                            "email"
                        );
                    }
                    // Hash password and PIN with bcrypt
                    String passwordHash = BcryptUtil.bcryptHash(req.password, 12);
                    String pinHash = BcryptUtil.bcryptHash(req.pin, 12);

                    // Validate PIN != password (simple check)
                    if (req.password.equals(req.pin)) {
                        throw SealException.badRequest(
                            "WEAK_PIN",
                            "PIN must not be the same as your password"
                        );
                    }

                    Pilgrim pilgrim = Pilgrim.forge(
                        req.phone, req.email, req.fullName, passwordHash, pinHash
                    );

                    return pilgrimRepo.persist(pilgrim);
                })
                .map(pilgrim -> Map.of(
                    "pilgrimId", pilgrim.id.toString(),
                    "phone", pilgrim.phone,
                    "status", pilgrim.status.name().toLowerCase(),
                    "attunedAt", pilgrim.attunedAt.toString()
                ));
    }

    // ─── 2. RITE OF RETURN (LOGIN) ────────────────────────────────────────────

    @WithTransaction
    public Uni<SealResponse> rite(RiteRequest req) {
        return crystalService.getLoginFailCount(req.phone)
                .flatMap(failCount -> {
                    if (failCount >= MAX_LOGIN_ATTEMPTS) {
                        throw SealException.tooManyRequests(
                            "LOGIN_LOCKED",
                            "Too many failed rites. The sanctum is sealed for 30 minutes."
                        );
                    }
                    return pilgrimRepo.findByPhone(req.phone);
                })
                .flatMap(pilgrimOpt -> {
                    Pilgrim pilgrim = pilgrimOpt.orElseThrow(() ->
                        SealException.unauthorized("INVALID_CREDENTIALS",
                            "The resonance does not match the seal")
                    );

                    if (pilgrim.isSanctioned()) {
                        throw SealException.forbidden("PILGRIM_SANCTIONED",
                            "Your seal has been sanctioned by the Oracle");
                    }

                    if (!BcryptUtil.matches(req.password, pilgrim.passwordHash)) {
                        return crystalService.incrementLoginFail(req.phone)
                                .flatMap(count -> {
                                    LOG.warnf("Failed rite for %s, attempt %d", req.phone, count);
                                    throw SealException.unauthorized("INVALID_CREDENTIALS",
                                        "The resonance does not match the seal");
                                });
                    }

                    // Password matches — clear fail counter, update lastRiteAt
                    pilgrim.lastRiteAt = Instant.now();

                    // Determine roles
                    Set<String> roles = pilgrim.isOracle
                        ? Set.of("Pilgrim", "Oracle")
                        : Set.of("Pilgrim");

                    String accessSeal = jwtService.issueAccessSeal(
                        pilgrim.id, pilgrim.phone, pilgrim.isOracle, roles
                    );
                    String refreshSeal = jwtService.issueRefreshSeal(
                        pilgrim.id, req.deviceId
                    );

                    // Persist session
                    Session session = Session.forge(
                        pilgrim.id,
                        jwtService.extractJti(refreshSeal),
                        req.deviceId,
                        Instant.now().plusSeconds(jwtService.getRefreshExpiry())
                    );

                    return crystalService.clearLoginFail(req.phone)
                            .flatMap(v -> sessionRepo.persist(session))
                            .flatMap(s -> pilgrimRepo.persist(pilgrim))
                            .map(p -> SealResponse.issued(
                                accessSeal,
                                refreshSeal,
                                jwtService.getAccessExpiry(),
                                PilgrimResponse.from(p)
                            ));
                });
    }

    // ─── 3. RENEW SEAL (REFRESH TOKEN) ────────────────────────────────────────

    @WithTransaction
    public Uni<SealResponse> renew(RenewRequest req) {
        String jti = jwtService.extractJti(req.refreshSeal);
        UUID pilgrimId = jwtService.extractPilgrimId(req.refreshSeal);

        return crystalService.isSanctioned(jti)
                .flatMap(sanctioned -> {
                    if (sanctioned) {
                        throw SealException.unauthorized("SEAL_REVOKED",
                            "Refresh seal has been revoked by the Priestess");
                    }
                    return sessionRepo.findByRefreshJti(jti);
                })
                .flatMap(sessionOpt -> {
                    Session session = sessionOpt.orElseThrow(() ->
                        SealException.unauthorized("SEAL_NOT_FOUND", "Refresh seal not recognized"));

                    if (!session.isActive()) {
                        throw SealException.unauthorized("SEAL_EXPIRED",
                            "Refresh seal has expired. Perform the Rite of Return again.");
                    }

                    return pilgrimRepo.findByIdSafe(pilgrimId);
                })
                .map(pilgrimOpt -> {
                    Pilgrim pilgrim = pilgrimOpt.orElseThrow(() ->
                        SealException.notFound("PILGRIM_NOT_FOUND", "Pilgrim not found"));

                    if (pilgrim.isSanctioned()) {
                        throw SealException.forbidden("PILGRIM_SANCTIONED",
                            "Your seal has been sanctioned by the Oracle");
                    }

                    Set<String> roles = pilgrim.isOracle
                        ? Set.of("Pilgrim", "Oracle")
                        : Set.of("Pilgrim");

                    String accessSeal = jwtService.issueAccessSeal(
                        pilgrim.id, pilgrim.phone, pilgrim.isOracle, roles
                    );
                    return SealResponse.refreshed(accessSeal, jwtService.getAccessExpiry());
                });
    }

    // ─── 4. SEVER SEAL (LOGOUT) ───────────────────────────────────────────────

    @WithTransaction
    public Uni<Void> severSeal(String accessSealJti, SeverRequest req) {
        String refreshJti = jwtService.extractJti(req.refreshSeal);
        long refreshTtl = jwtService.getRefreshExpiry();

        return sessionRepo.findByRefreshJti(refreshJti)
                .flatMap(sessionOpt -> {
                    if (sessionOpt.isEmpty()) {
                        // Already severed or not found — silently succeed
                        return Uni.createFrom().voidItem();
                    }
                    Session session = sessionOpt.get();
                    session.sever("SEVERED");

                    return sessionRepo.persist(session)
                            .flatMap(s -> crystalService.sanctionSeal(refreshJti, refreshTtl))
                            .flatMap(v -> accessSealJti != null
                                ? crystalService.sanctionSeal(accessSealJti, jwtService.getAccessExpiry())
                                : Uni.createFrom().voidItem());
                });
    }

    // ─── 5. TRANSMUTE PIN (CHANGE PIN) ────────────────────────────────────────

    @WithTransaction
    public Uni<Void> transmutePin(UUID pilgrimId, TransmutePinRequest req) {
        return pilgrimRepo.findByIdSafe(pilgrimId)
                .flatMap(pilgrimOpt -> {
                    Pilgrim pilgrim = pilgrimOpt.orElseThrow(() ->
                        SealException.notFound("PILGRIM_NOT_FOUND", "Pilgrim not found"));

                    if (pilgrim.isPinLocked()) {
                        throw SealException.tooManyRequests("PIN_LOCKED",
                            "PIN is locked due to too many failed attempts. Try again in 30 minutes.");
                    }

                    if (!BcryptUtil.matches(req.oldPin, pilgrim.pinHash)) {
                        return crystalService.incrementPinFail(pilgrimId.toString())
                                .flatMap(count -> {
                                    if (count >= MAX_PIN_ATTEMPTS) {
                                        pilgrim.pinLockedUntil = Instant.now().plusSeconds(1800);
                                        return pilgrimRepo.persist(pilgrim)
                                                .flatMap(p -> Uni.createFrom().failure(
                                                    SealException.tooManyRequests("PIN_LOCKED",
                                                        "PIN locked for 30 minutes after 5 failed attempts")));
                                    }
                                    throw SealException.badRequest("INVALID_PIN",
                                        "Old PIN is incorrect. " + (MAX_PIN_ATTEMPTS - count) + " attempt(s) remaining.");
                                });
                    }

                    // Old PIN correct — update to new PIN
                    pilgrim.pinHash = BcryptUtil.bcryptHash(req.newPin, 12);
                    pilgrim.pinFailedAttempts = 0;
                    pilgrim.pinLockedUntil = null;

                    return crystalService.clearPinFail(pilgrimId.toString())
                            .flatMap(v -> pilgrimRepo.persist(pilgrim))
                            .replaceWithVoid();
                });
    }

    // ─── 6. GAZE UPON SELF (GET PROFILE) ─────────────────────────────────────

    public Uni<PilgrimResponse> getSelf(UUID pilgrimId) {
        return pilgrimRepo.findByIdSafe(pilgrimId)
                .map(pilgrimOpt -> {
                    Pilgrim pilgrim = pilgrimOpt.orElseThrow(() ->
                        SealException.notFound("PILGRIM_NOT_FOUND", "Pilgrim not found"));
                    return PilgrimResponse.from(pilgrim);
                });
    }

    // ─── 7. UPDATE SELF (UPDATE PROFILE) ─────────────────────────────────────

    @WithTransaction
    public Uni<PilgrimResponse> updateSelf(UUID pilgrimId, UpdatePilgrimRequest req) {
        return pilgrimRepo.findByIdSafe(pilgrimId)
                .flatMap(pilgrimOpt -> {
                    Pilgrim pilgrim = pilgrimOpt.orElseThrow(() ->
                        SealException.notFound("PILGRIM_NOT_FOUND", "Pilgrim not found"));

                    if (req.fullName != null) pilgrim.fullName = req.fullName;
                    if (req.email != null) pilgrim.email = req.email;
                    if (req.profilePhotoUrl != null) pilgrim.profilePhotoUrl = req.profilePhotoUrl;

                    return pilgrimRepo.persist(pilgrim);
                })
                .map(PilgrimResponse::from);
    }

    // ─── 8. ORACLE: SANCTION SEAL (INSTA-BAN) ────────────────────────────────

    @WithTransaction
    public Uni<Map<String, Object>> sanctionPilgrim(UUID oracleId, SanctionRequest req) {
        return pilgrimRepo.findByIdSafe(req.pilgrimId)
                .flatMap(pilgrimOpt -> {
                    Pilgrim target = pilgrimOpt.orElseThrow(() ->
                        SealException.notFound("PILGRIM_NOT_FOUND", "Target pilgrim not found"));

                    if (target.isOracle) {
                        throw SealException.forbidden("CANNOT_SANCTION_ORACLE",
                            "The Priestess herself cannot be sanctioned");
                    }

                    target.status = Pilgrim.PilgrimStatus.SANCTIONED;

                    Instant sanctionedUntil = null;
                    if ("TEMPORARY".equals(req.sanctionDuration) && req.durationDays != null) {
                        sanctionedUntil = Instant.now().plusSeconds(req.durationDays * 86400L);
                    }

                    SanctionRecord record = SanctionRecord.forge(
                        req.pilgrimId, oracleId,
                        req.reason, req.description,
                        req.sanctionDuration, sanctionedUntil
                    );

                    return sessionRepo.severAllByPilgrimId(req.pilgrimId, "SANCTIONED")
                            .flatMap(revokedCount -> {
                                // Add active seals to Crystal blacklist
                                return crystalService.sanctionSeal(
                                    "pilgrim:" + req.pilgrimId, jwtService.getAccessExpiry()
                                ).map(v -> revokedCount);
                            })
                            .flatMap(revokedCount ->
                                pilgrimRepo.persist(target)
                                    .flatMap(p -> SanctionRecord.persist(record))
                                    .map(sr -> {
                                        // Publish RabbitMQ event
                                        sanctionedEmitter.send(new PilgrimSanctionedEvent(
                                            req.pilgrimId.toString(),
                                            req.reason
                                        ));
                                        return Map.of(
                                            "pilgrimId", req.pilgrimId.toString(),
                                            "status", "sanctioned",
                                            "sanctionedAt", Instant.now().toString(),
                                            "reason", req.reason,
                                            "sealsSevered", revokedCount
                                        );
                                    })
                            );
                });
    }

    // ─── PIN VERIFICATION (used by other sanctums via internal endpoint) ──────

    public Uni<Boolean> verifyPin(UUID pilgrimId, String pin) {
        return pilgrimRepo.findByIdSafe(pilgrimId)
                .map(pilgrimOpt -> {
                    Pilgrim pilgrim = pilgrimOpt.orElseThrow(() ->
                        SealException.notFound("PILGRIM_NOT_FOUND", "Pilgrim not found"));
                    if (pilgrim.isPinLocked()) {
                        throw SealException.tooManyRequests("PIN_LOCKED",
                            "PIN is locked due to too many failed attempts");
                    }
                    return BcryptUtil.matches(pin, pilgrim.pinHash);
                });
    }
}
