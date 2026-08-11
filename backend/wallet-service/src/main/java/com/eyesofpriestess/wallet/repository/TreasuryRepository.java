package com.eyesofpriestess.wallet.repository;

import com.eyesofpriestess.wallet.entity.Treasury;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

/** TreasuryRepository — Data access for Treasury entities. */
@ApplicationScoped
public class TreasuryRepository implements PanacheRepositoryBase<Treasury, UUID> {

    public Uni<Optional<Treasury>> findByPilgrimId(UUID pilgrimId) {
        return find("pilgrimId", pilgrimId)
                .firstResult()
                .map(Optional::ofNullable);
    }

    /** Find treasury with pessimistic read for balance operations. */
    public Uni<Optional<Treasury>> findByPilgrimIdForUpdate(UUID pilgrimId) {
        return find("pilgrimId = ?1 AND status = ?2",
                pilgrimId, Treasury.TreasuryStatus.ACTIVE)
                .firstResult()
                .map(Optional::ofNullable);
    }

    public Uni<Optional<Treasury>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }
}
