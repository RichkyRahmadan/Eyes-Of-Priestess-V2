package com.eyesofpriestess.room.repository;

import com.eyesofpriestess.room.entity.CovenantInvitation;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class InvitationRepository implements PanacheRepositoryBase<CovenantInvitation, UUID> {

    public Uni<Optional<CovenantInvitation>> findByCode(String code) {
        return find("invitationCode", code).firstResult().map(Optional::ofNullable);
    }

    public Uni<Optional<CovenantInvitation>> findByCovenantId(UUID covenantId) {
        return find("covenantId", covenantId).firstResult().map(Optional::ofNullable);
    }
}
