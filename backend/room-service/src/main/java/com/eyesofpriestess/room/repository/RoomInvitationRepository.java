package com.eyesofpriestess.room.repository;

import com.eyesofpriestess.room.entity.RoomInvitation;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RoomInvitationRepository implements PanacheRepositoryBase<RoomInvitation, UUID> {

    public Uni<Optional<RoomInvitation>> findByCode(String code) {
        return find("invitationCode", code).firstResult().map(Optional::ofNullable);
    }

    public Uni<Optional<RoomInvitation>> findByCovenantId(UUID roomId) {
        return find("roomId", roomId).firstResult().map(Optional::ofNullable);
    }
}
