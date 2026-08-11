package com.eyesofpriestess.wallet.repository;

import com.eyesofpriestess.wallet.entity.Chronicle;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** ChronicleRepository — Append-only access to transaction history. */
@ApplicationScoped
public class ChronicleRepository implements PanacheRepositoryBase<Chronicle, UUID> {

    /** Paginated chronicle history for a treasury, newest first. */
    public Uni<List<Chronicle>> findByTreasuryId(UUID treasuryId, int page, int size) {
        return find("treasuryId = ?1", Sort.by("createdAt").descending(), treasuryId)
                .page(Page.of(page, size))
                .list();
    }

    public Uni<Long> countByTreasuryId(UUID treasuryId) {
        return count("treasuryId", treasuryId);
    }

    public Uni<Optional<Chronicle>> findByCovenantKey(String covenantKey) {
        return find("covenantKey", covenantKey)
                .firstResult()
                .map(Optional::ofNullable);
    }
}
