package com.eyesofpriestess.auth.repository;

import com.eyesofpriestess.auth.entity.Session;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * SessionRepository — Reactive data access for Session (refresh seal tracking).
 */
@ApplicationScoped
public class SessionRepository implements PanacheRepositoryBase<Session, UUID> {

    /** Find session by refresh seal JTI (for renewal & revocation). */
    public Uni<Optional<Session>> findByRefreshJti(String jti) {
        return find("refreshSealJti", jti)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /** Find all active sessions for a User (for sanction revocation). */
    public Uni<List<Session>> findActiveByPilgrimId(UUID userId) {
        return list("userId = ?1 AND severedAt IS NULL", userId);
    }

    /** Count active sessions for a User. */
    public Uni<Long> countActiveByPilgrimId(UUID userId) {
        return count("userId = ?1 AND severedAt IS NULL", userId);
    }

    /** Mark all sessions for a User as severed (used on sanction). */
    public Uni<Long> severAllByPilgrimId(UUID userId, String reason) {
        return update(
            "severedAt = NOW(), severedReason = ?1 WHERE userId = ?2 AND severedAt IS NULL",
            reason, userId
        ).map(Integer::longValue);
    }
}
