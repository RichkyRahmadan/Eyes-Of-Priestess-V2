package com.eyesofpriestess.covenant.repository;

import com.eyesofpriestess.covenant.entity.Covenant;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class CovenantRepository implements PanacheRepositoryBase<Covenant, UUID> {

    public Uni<Optional<Covenant>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }

    public Uni<List<Covenant>> findByPilgrimId(UUID pilgrimId, int page, int size) {
        return find("initiatorId = ?1 or counterpartyId = ?1 order by createdAt desc", pilgrimId)
                .page(page, size)
                .list();
    }

    public Uni<List<Covenant>> findDeliveredPastAutoRelease(Instant cutoffTime) {
        return list("status = ?1 and autoReleaseAt <= ?2", Covenant.CovenantStatus.DELIVERED, cutoffTime);
    }
}
