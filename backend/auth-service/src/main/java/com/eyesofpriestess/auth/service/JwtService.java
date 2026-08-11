package com.eyesofpriestess.auth.service;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

/**
 * JwtService — Sacred Seal (JWT) generation and validation.
 *
 * Access Seal: short-lived (15 min), contains pilgrim identity
 * Refresh Seal: long-lived (7 days), used only for renewal
 *
 * Uses SmallRye JWT Build with RSA private key for signing.
 */
@ApplicationScoped
public class JwtService {

    @ConfigProperty(name = "jwt.access.expiry", defaultValue = "900")
    long accessExpiry;

    @ConfigProperty(name = "jwt.refresh.expiry", defaultValue = "604800")
    long refreshExpiry;

    @ConfigProperty(name = "jwt.issuer", defaultValue = "eyesofpriestess")
    String issuer;

    /**
     * Issues a new Sacred Seal (access JWT) for the given Pilgrim.
     */
    public String issueAccessSeal(UUID pilgrimId, String phone,
                                  boolean isOracle, Set<String> roles) {
        Instant now = Instant.now();
        return Jwt.issuer(issuer)
                .subject(pilgrimId.toString())
                .claim("phone", phone)
                .claim("oracle", isOracle)
                .claim("typ", "access")
                .groups(roles)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(accessExpiry))
                .jws()
                .sign();
    }

    /**
     * Issues a new Refresh Seal (long-lived JWT) for session management.
     */
    public String issueRefreshSeal(UUID pilgrimId, String deviceId) {
        Instant now = Instant.now();
        String jti = UUID.randomUUID().toString();
        return Jwt.issuer(issuer)
                .subject(pilgrimId.toString())
                .claim("typ", "refresh")
                .claim("jti", jti)
                .claim("deviceId", deviceId)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(refreshExpiry))
                .jws()
                .sign();
    }

    /**
     * Extracts the JTI (JWT ID) from a token string without full validation.
     * Used to add token to Crystal sanction list on sever/sanction.
     */
    public String extractJti(String token) {
        try {
            // Decode payload without signature verification (we trust our own tokens)
            String[] parts = token.split("\\.");
            if (parts.length != 3) return UUID.randomUUID().toString();
            String payload = new String(
                java.util.Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
            );
            // Parse jti from payload JSON (simple extraction)
            if (payload.contains("\"jti\"")) {
                int start = payload.indexOf("\"jti\"") + 7;
                int end = payload.indexOf("\"", start + 1);
                return payload.substring(start + 1, end);
            }
            // Fallback: use sub+iat as key
            return UUID.randomUUID().toString();
        } catch (Exception e) {
            return UUID.randomUUID().toString();
        }
    }

    /**
     * Extracts the pilgrim UUID from a token's subject claim.
     */
    public UUID extractPilgrimId(String token) {
        try {
            String[] parts = token.split("\\.");
            String payload = new String(
                java.util.Base64.getUrlDecoder().decode(parts[1]),
                StandardCharsets.UTF_8
            );
            int start = payload.indexOf("\"sub\"") + 7;
            int end = payload.indexOf("\"", start + 1);
            return UUID.fromString(payload.substring(start + 1, end));
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid seal token");
        }
    }

    public long getAccessExpiry() { return accessExpiry; }
    public long getRefreshExpiry() { return refreshExpiry; }
}
