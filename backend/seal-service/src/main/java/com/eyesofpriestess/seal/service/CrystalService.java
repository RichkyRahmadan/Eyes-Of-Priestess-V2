package com.eyesofpriestess.seal.service;

import io.quarkus.redis.datasource.ReactiveRedisDataSource;
import io.quarkus.redis.datasource.value.ReactiveValueCommands;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.Duration;

/**
 * CrystalService — The Crystal (Redis) operations for the Seal Sanctum.
 *
 * Manages:
 * - Sanction Seal blacklist: `sanction:<jti>` → TTL = remaining JWT expiry
 * - OTP (Omen) storage: `omen:<token>` → OTP code, TTL = 5 min
 * - Login rate limit: `login_fail:<phone>` → fail count, TTL = 30 min
 */
@ApplicationScoped
public class CrystalService {

    private static final String SANCTION_PREFIX = "sanction:";
    private static final String OMEN_PREFIX = "omen:";
    private static final String LOGIN_FAIL_PREFIX = "login_fail:";
    private static final String PIN_FAIL_PREFIX = "pin_fail:";

    @Inject
    ReactiveRedisDataSource redisDs;

    private ReactiveValueCommands<String, String> values() {
        return redisDs.value(String.class);
    }

    // ─── Sanction Seal (Token Blacklist) ─────────────────────────────────────

    /**
     * Adds a JTI to the Crystal sanction blacklist.
     * @param jti JWT token ID to blacklist
     * @param ttlSeconds remaining seconds until the JWT would have expired
     */
    public Uni<Void> sanctionSeal(String jti, long ttlSeconds) {
        return values()
                .setex(SANCTION_PREFIX + jti, ttlSeconds, "SANCTIONED")
                .replaceWithVoid();
    }

    /**
     * Checks if a JTI is on the Crystal sanction blacklist.
     */
    public Uni<Boolean> isSanctioned(String jti) {
        return values()
                .get(SANCTION_PREFIX + jti)
                .map(val -> val != null);
    }

    // ─── Omen (OTP) Management ────────────────────────────────────────────────

    /**
     * Stores an OTP code associated with a token. TTL = 5 minutes.
     */
    public Uni<Void> storeOmen(String omenToken, String otpCode, String phone) {
        String payload = otpCode + ":" + phone;
        return values()
                .setex(OMEN_PREFIX + omenToken, 300, payload)
                .replaceWithVoid();
    }

    /**
     * Retrieves and validates an Omen token.
     * @return the phone number if valid, null if expired or not found
     */
    public Uni<String> verifyOmen(String omenToken, String otpCode) {
        return values()
                .get(OMEN_PREFIX + omenToken)
                .map(payload -> {
                    if (payload == null) return null;
                    String[] parts = payload.split(":");
                    if (parts.length != 2) return null;
                    if (!parts[0].equals(otpCode)) return null;
                    return parts[1]; // phone
                });
    }

    /**
     * Issues a verification token after successful OTP verification.
     * TTL = 15 minutes (must be used within this window to register).
     */
    public Uni<Void> storeVerificationToken(String verificationToken, String phone) {
        return values()
                .setex("verified:" + verificationToken, 900, phone)
                .replaceWithVoid();
    }

    public Uni<String> getVerifiedPhone(String verificationToken) {
        return values().get("verified:" + verificationToken);
    }

    public Uni<Void> invalidateVerificationToken(String verificationToken) {
        return redisDs.key(String.class)
                .del("verified:" + verificationToken)
                .replaceWithVoid();
    }

    // ─── Login Rate Limiting ──────────────────────────────────────────────────

    /**
     * Increments login failure count for a phone. TTL reset to 30 min each time.
     * @return new failure count
     */
    public Uni<Long> incrementLoginFail(String phone) {
        String key = LOGIN_FAIL_PREFIX + phone;
        return values().incr(key)
                .call(count -> {
                    // Extend TTL on each failure (30 min window)
                    return values().expire(key, Duration.ofMinutes(30));
                });
    }

    public Uni<Void> clearLoginFail(String phone) {
        return redisDs.key(String.class)
                .del(LOGIN_FAIL_PREFIX + phone)
                .replaceWithVoid();
    }

    public Uni<Long> getLoginFailCount(String phone) {
        return values().get(LOGIN_FAIL_PREFIX + phone)
                .map(val -> val == null ? 0L : Long.parseLong(val));
    }

    // ─── PIN Rate Limiting ────────────────────────────────────────────────────

    public Uni<Long> incrementPinFail(String pilgrimId) {
        String key = PIN_FAIL_PREFIX + pilgrimId;
        return values().incr(key)
                .call(count -> values().expire(key, Duration.ofMinutes(30)));
    }

    public Uni<Void> clearPinFail(String pilgrimId) {
        return redisDs.key(String.class)
                .del(PIN_FAIL_PREFIX + pilgrimId)
                .replaceWithVoid();
    }
}
