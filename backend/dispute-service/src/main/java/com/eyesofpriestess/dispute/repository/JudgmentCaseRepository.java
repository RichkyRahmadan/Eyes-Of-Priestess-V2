package com.eyesofpriestess.dispute.repository;

import com.eyesofpriestess.dispute.entity.JudgmentCase;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class JudgmentCaseRepository implements PanacheRepositoryBase<JudgmentCase, UUID> {

    public Uni<Optional<JudgmentCase>> findByCovenantId(UUID covenantId) {
        return find("covenantId", covenantId).firstResult().map(Optional::ofNullable);
    }

    public Uni<Optional<JudgmentCase>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }

    public Uni<List<JudgmentCase>> findByStatus(JudgmentCase.CaseStatus status, int page, int size) {
        return find("status = ?1 order by openedAt asc", status)
                .page(page, size)
                .list();
    }

    public Uni<List<JudgmentCase>> findByPilgrimId(UUID pilgrimId) {
        return list("initiatorId = ?1 or counterpartyId = ?1 order by openedAt desc", pilgrimId);
    }
}
