package com.eyesofpriestess.dispute.repository;

import com.eyesofpriestess.dispute.entity.DisputeCase;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class DisputeCaseRepository implements PanacheRepositoryBase<DisputeCase, UUID> {

    public Uni<Optional<DisputeCase>> findByCovenantId(UUID roomId) {
        return find("roomId", roomId).firstResult().map(Optional::ofNullable);
    }

    public Uni<Optional<DisputeCase>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }

    public Uni<List<DisputeCase>> findByStatus(DisputeCase.CaseStatus status, int page, int size) {
        return find("status = ?1 order by openedAt asc", status)
                .page(page, size)
                .list();
    }

    public Uni<List<DisputeCase>> findByPilgrimId(UUID userId) {
        return list("initiatorId = ?1 or counterpartyId = ?1 order by openedAt desc", userId);
    }

    public Uni<List<DisputeCase>> findAllPaged(int page, int size) {
        return findAll().page(page, size).list();
    }
}

