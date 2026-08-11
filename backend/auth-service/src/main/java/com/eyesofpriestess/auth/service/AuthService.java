package com.eyesofpriestess.auth.service;

import com.eyesofpriestess.auth.dto.request.*;
import com.eyesofpriestess.auth.dto.response.UserResponse;
import com.eyesofpriestess.auth.dto.response.AuthResponse;
import com.eyesofpriestess.auth.entity.User;
import com.eyesofpriestess.auth.entity.BanLog;
import com.eyesofpriestess.auth.entity.Session;
import com.eyesofpriestess.auth.event.UserBannedEvent;
import com.eyesofpriestess.auth.exception.AuthException;
import com.eyesofpriestess.auth.repository.UserRepository;
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
 * AuthService — Core business logic for the Seal Sanctum.
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
public class AuthService {

    private static final Logger LOG = Logger.getLogger(AuthService.class);
    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int MAX_PIN_ATTEMPTS = 5;

    @Inject UserRepository pilgrimRepo;
    @Inject SessionRepository sessionRepo;
    @Inject JwtService jwtService;
    @Inject RedisService RedisService;
    @Inject OtpService OtpService;

    @Inject
    @Channel("User-sanctioned")
    Emitter<UserBannedEvent> sanctionedEmitter;

    // ─── 1. FORGE IDENTITY (REGISTRATION) ─────────────────────────────────────

    @WithTransaction
    public Uni<Map<String, Object>> forgeIdentity(RegisterRequest req) {
        return pilgrimRepo.existsByPhone(req.phone)
                .flatMap(phoneExists -> {
                    if (phoneExists) {
                        throw AuthException.conflict(
                            "PHONE_ALREADY_ATTUNED",
                            "This resonance is already attuned to another User",
                            "phone"
                        );
                    }
                    return req.email != null
                        ? pilgrimRepo.existsByEmail(req.email)
                        : Uni.createFrom().item(false);
                })
                .flatMap(emailExists -> {
                    if (emailExists) {
                        throw AuthException.conflict(
                            "EMAIL_ALREADY_ATTUNED",
                            "This email is already bound to another User",
                            "email"
                        );
                    }
                    // Hash password and PIN with bcrypt
                    String passwordHash = BcryptUtil.bcryptHash(req.password, 12);
                    String pinHash = BcryptUtil.bcryptHash(req.pin, 12);

                    // Validate PIN != password (simple check)
                    if (req.password.equals(req.pin)) {
                        throw AuthException.badRequest(
                            "WEAK_PIN",
                            "PIN must not be the same as your password"
                        );
                    }

                    User user = User.forge(
                        req.phone, req.email, req.fullName, passwordHash, pinHash
                    );

                    return pilgrimRepo.persist(user);
                })
                .map(user -> Map.of(
                    "userId", user.id.toString(),
                    "phone", user.phone,
                    "status", user.status.name().toLowerCase(),
                    "attunedAt", user.attunedAt.toString()
                ));
    }

    // ─── 2. RITE OF RETURN (LOGIN) ────────────────────────────────────────────

    @WithTransaction
    public Uni<AuthResponse> rite(LoginRequest req) {
        return RedisService.getLoginFailCount(req.phone)
                .flatMap(failCount -> {
                    if (failCount >= MAX_LOGIN_ATTEMPTS) {
                        throw AuthException.tooManyRequests(
                            "LOGIN_LOCKED",
                            "Too many failed rites. The sanctum is sealed for 30 minutes."
                        );
                    }
                    return pilgrimRepo.findByPhone(req.phone);
                })
                .flatMap(pilgrimOpt -> {
                    User user = pilgrimOpt.orElseThrow(() ->
                        AuthException.unauthorized("INVALID_CREDENTIALS",
                            "The resonance does not match the seal")
                    );

                    if (user.isSanctioned()) {
                        throw AuthException.forbidden("PILGRIM_SANCTIONED",
                            "Your seal has been sanctioned by the Oracle");
                    }

                    if (!BcryptUtil.matches(req.password, user.passwordHash)) {
                        return RedisService.incrementLoginFail(req.phone)
                                .flatMap(count -> {
                                    LOG.warnf("Failed rite for %s, attempt %d", req.phone, count);
                                    throw AuthException.unauthorized("INVALID_CREDENTIALS",
                                        "The resonance does not match the seal");
                                });
                    }

                    // Password matches — clear fail counter, update lastRiteAt
                    user.lastRiteAt = Instant.now();

                    // Determine roles
                    Set<String> roles = user.isOracle
                        ? Set.of("User", "Oracle")
                        : Set.of("User");

                    String accessSeal = jwtService.issueAccessSeal(
                        user.id, user.phone, user.isOracle, roles
                    );
                    String refreshSeal = jwtService.issueRefreshSeal(
                        user.id, req.deviceId
                    );

                    // Persist session
                    Session session = Session.forge(
                        user.id,
                        jwtService.extractJti(refreshSeal),
                        req.deviceId,
                        Instant.now().plusSeconds(jwtService.getRefreshExpiry())
                    );

                    return RedisService.clearLoginFail(req.phone)
                            .flatMap(v -> sessionRepo.persist(session))
                            .flatMap(s -> pilgrimRepo.persist(user))
                            .map(p -> AuthResponse.issued(
                                accessSeal,
                                refreshSeal,
                                jwtService.getAccessExpiry(),
                                UserResponse.from(p)
                            ));
                });
    }

    // ─── 3. RENEW SEAL (REFRESH TOKEN) ────────────────────────────────────────

    @WithTransaction
    public Uni<AuthResponse> renew(RefreshTokenRequest req) {
        String jti = jwtService.extractJti(req.refreshSeal);
        UUID userId = jwtService.extractPilgrimId(req.refreshSeal);

        return RedisService.isSanctioned(jti)
                .flatMap(sanctioned -> {
                    if (sanctioned) {
                        throw AuthException.unauthorized("SEAL_REVOKED",
                            "Refresh seal has been revoked by the Priestess");
                    }
                    return sessionRepo.findByRefreshJti(jti);
                })
                .flatMap(sessionOpt -> {
                    Session session = sessionOpt.orElseThrow(() ->
                        AuthException.unauthorized("SEAL_NOT_FOUND", "Refresh seal not recognized"));

                    if (!session.isActive()) {
                        throw AuthException.unauthorized("SEAL_EXPIRED",
                            "Refresh seal has expired. Perform the Rite of Return again.");
                    }

                    return pilgrimRepo.findByIdSafe(userId);
                })
                .map(pilgrimOpt -> {
                    User user = pilgrimOpt.orElseThrow(() ->
                        AuthException.notFound("PILGRIM_NOT_FOUND", "User not found"));

                    if (user.isSanctioned()) {
                        throw AuthException.forbidden("PILGRIM_SANCTIONED",
                            "Your seal has been sanctioned by the Oracle");
                    }

                    Set<String> roles = user.isOracle
                        ? Set.of("User", "Oracle")
                        : Set.of("User");

                    String accessSeal = jwtService.issueAccessSeal(
                        user.id, user.phone, user.isOracle, roles
                    );
                    return AuthResponse.refreshed(accessSeal, jwtService.getAccessExpiry());
                });
    }

    // ─── 4. SEVER SEAL (LOGOUT) ───────────────────────────────────────────────

    @WithTransaction
    public Uni<Void> severSeal(String accessSealJti, LogoutRequest req) {
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
                            .flatMap(s -> RedisService.sanctionSeal(refreshJti, refreshTtl))
                            .flatMap(v -> accessSealJti != null
                                ? RedisService.sanctionSeal(accessSealJti, jwtService.getAccessExpiry())
                                : Uni.createFrom().voidItem());
                });
    }

    // ─── 5. TRANSMUTE PIN (CHANGE PIN) ────────────────────────────────────────

    @WithTransaction
    public Uni<Void> transmutePin(UUID userId, SetPinRequest req) {
        return pilgrimRepo.findByIdSafe(userId)
                .flatMap(pilgrimOpt -> {
                    User user = pilgrimOpt.orElseThrow(() ->
                        AuthException.notFound("PILGRIM_NOT_FOUND", "User not found"));

                    if (user.isPinLocked()) {
                        throw AuthException.tooManyRequests("PIN_LOCKED",
                            "PIN is locked due to too many failed attempts. Try again in 30 minutes.");
                    }

                    if (!BcryptUtil.matches(req.oldPin, user.pinHash)) {
                        return RedisService.incrementPinFail(userId.toString())
                                .flatMap(count -> {
                                    if (count >= MAX_PIN_ATTEMPTS) {
                                        user.pinLockedUntil = Instant.now().plusSeconds(1800);
                                        return pilgrimRepo.persist(user)
                                                .flatMap(p -> Uni.createFrom().failure(
                                                    AuthException.tooManyRequests("PIN_LOCKED",
                                                        "PIN locked for 30 minutes after 5 failed attempts")));
                                    }
                                    throw AuthException.badRequest("INVALID_PIN",
                                        "Old PIN is incorrect. " + (MAX_PIN_ATTEMPTS - count) + " attempt(s) remaining.");
                                });
                    }

                    // Old PIN correct — update to new PIN
                    user.pinHash = BcryptUtil.bcryptHash(req.newPin, 12);
                    user.pinFailedAttempts = 0;
                    user.pinLockedUntil = null;

                    return RedisService.clearPinFail(userId.toString())
                            .flatMap(v -> pilgrimRepo.persist(user))
                            .replaceWithVoid();
                });
    }

    // ─── 6. GAZE UPON SELF (GET PROFILE) ─────────────────────────────────────

    public Uni<UserResponse> getSelf(UUID userId) {
        return pilgrimRepo.findByIdSafe(userId)
                .map(pilgrimOpt -> {
                    User user = pilgrimOpt.orElseThrow(() ->
                        AuthException.notFound("PILGRIM_NOT_FOUND", "User not found"));
                    return UserResponse.from(user);
                });
    }

    // ─── 7. UPDATE SELF (UPDATE PROFILE) ─────────────────────────────────────

    @WithTransaction
    public Uni<UserResponse> updateSelf(UUID userId, UpdateUserRequest req) {
        return pilgrimRepo.findByIdSafe(userId)
                .flatMap(pilgrimOpt -> {
                    User user = pilgrimOpt.orElseThrow(() ->
                        AuthException.notFound("PILGRIM_NOT_FOUND", "User not found"));

                    if (req.fullName != null) user.fullName = req.fullName;
                    if (req.email != null) user.email = req.email;
                    if (req.profilePhotoUrl != null) user.profilePhotoUrl = req.profilePhotoUrl;

                    return pilgrimRepo.persist(user);
                })
                .map(UserResponse::from);
    }

    // ─── 8. ORACLE: SANCTION SEAL (INSTA-BAN) ────────────────────────────────

    @WithTransaction
    public Uni<Map<String, Object>> sanctionPilgrim(UUID oracleId, BanUserRequest req) {
        return pilgrimRepo.findByIdSafe(req.userId)
                .flatMap(pilgrimOpt -> {
                    User target = pilgrimOpt.orElseThrow(() ->
                        AuthException.notFound("PILGRIM_NOT_FOUND", "Target User not found"));

                    if (target.isOracle) {
                        throw AuthException.forbidden("CANNOT_SANCTION_ORACLE",
                            "The Priestess herself cannot be sanctioned");
                    }

                    target.status = User.PilgrimStatus.SANCTIONED;

                    Instant sanctionedUntil = null;
                    if ("TEMPORARY".equals(req.sanctionDuration) && req.durationDays != null) {
                        sanctionedUntil = Instant.now().plusSeconds(req.durationDays * 86400L);
                    }

                    BanLog record = BanLog.forge(
                        req.userId, oracleId,
                        req.reason, req.description,
                        req.sanctionDuration, sanctionedUntil
                    );

                    return sessionRepo.severAllByPilgrimId(req.userId, "SANCTIONED")
                            .flatMap(revokedCount -> {
                                // Add active seals to Crystal blacklist
                                return RedisService.sanctionSeal(
                                    "User:" + req.userId, jwtService.getAccessExpiry()
                                ).map(v -> revokedCount);
                            })
                            .flatMap(revokedCount ->
                                pilgrimRepo.persist(target)
                                    .flatMap(p -> BanLog.persist(record))
                                    .map(sr -> {
                                        // Publish RabbitMQ event
                                        sanctionedEmitter.send(new UserBannedEvent(
                                            req.userId.toString(),
                                            req.reason
                                        ));
                                        return Map.of(
                                            "userId", req.userId.toString(),
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

    public Uni<Boolean> verifyPin(UUID userId, String pin) {
        return pilgrimRepo.findByIdSafe(userId)
                .map(pilgrimOpt -> {
                    User user = pilgrimOpt.orElseThrow(() ->
                        AuthException.notFound("PILGRIM_NOT_FOUND", "User not found"));
                    if (user.isPinLocked()) {
                        throw AuthException.tooManyRequests("PIN_LOCKED",
                            "PIN is locked due to too many failed attempts");
                    }
                    return BcryptUtil.matches(pin, user.pinHash);
                });
    }
}
