package com.eyesofpriestess.seal.repository;

import com.eyesofpriestess.seal.entity.Session;
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

    /** Find all active sessions for a pilgrim (for sanction revocation). */
    public Uni<List<Session>> findActiveByPilgrimId(UUID pilgrimId) {
        return list("pilgrimId = ?1 AND severedAt IS NULL", pilgrimId);
    }

    /** Count active sessions for a pilgrim. */
    public Uni<Long> countActiveByPilgrimId(UUID pilgrimId) {
        return count("pilgrimId = ?1 AND severedAt IS NULL", pilgrimId);
    }

    /** Mark all sessions for a pilgrim as severed (used on sanction). */
    public Uni<Long> severAllByPilgrimId(UUID pilgrimId, String reason) {
        return update(
            "severedAt = NOW(), severedReason = ?1 WHERE pilgrimId = ?2 AND severedAt IS NULL",
            reason, pilgrimId
        );
    }
}
