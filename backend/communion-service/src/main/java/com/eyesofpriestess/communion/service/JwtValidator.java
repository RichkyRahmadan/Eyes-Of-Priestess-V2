package com.eyesofpriestess.communion.service;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.jwt.JsonWebToken;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.jboss.logging.Logger;

/**
 * JwtValidator — Validates a JWT token string during WebSocket handshake (query param `token`).
 * Extracts pilgrimId and display name for injection into WebSocket session user properties.
 */
@ApplicationScoped
public class JwtValidator {

    private static final Logger LOG = Logger.getLogger(JwtValidator.class);

    @Inject JWTParser jwtParser;

    @ConfigProperty(name = "mp.jwt.verify.issuer", defaultValue = "eyesofpriestess")
    String expectedIssuer;

    /**
     * Validates a raw JWT string. Returns the parsed token on success, null on failure.
     */
    public JsonWebToken validate(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return null;
        try {
            return jwtParser.parse(rawToken);
        } catch (Exception e) {
            LOG.warnf("[JWT-VALIDATOR] Invalid token: %s", e.getMessage());
            return null;
        }
    }

    /**
     * Stamps pilgrim identity onto a WebSocket Session's user properties.
     */
    public boolean stampSession(Session session, String rawToken) {
        JsonWebToken jwt = validate(rawToken);
        if (jwt == null) return false;
        session.getUserProperties().put("pilgrimId", java.util.UUID.fromString(jwt.getSubject()));
        session.getUserProperties().put("display", jwt.getClaim("display") != null
                ? (String) jwt.getClaim("display")
                : "Pilgrim-" + jwt.getSubject().substring(0, 6));
        return true;
    }
}
