package com.eyesofpriestess.room.repository;

import com.eyesofpriestess.room.entity.Room;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class RoomRepository implements PanacheRepositoryBase<Room, UUID> {

    public Uni<Optional<Room>> findByIdSafe(UUID id) {
        return findById(id).map(Optional::ofNullable);
    }

    public Uni<List<Room>> findByPilgrimId(UUID userId, int page, int size) {
        return find("initiatorId = ?1 or counterpartyId = ?1 order by createdAt desc", userId)
                .page(page, size)
                .list();
    }

    public Uni<List<Room>> findDeliveredPastAutoRelease(Instant cutoffTime) {
        return list("status = ?1 and autoReleaseAt <= ?2", Room.CovenantStatus.DELIVERED, cutoffTime);
    }
}
