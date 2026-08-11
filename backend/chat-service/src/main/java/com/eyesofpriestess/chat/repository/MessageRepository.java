package com.eyesofpriestess.chat.repository;

import com.eyesofpriestess.chat.entity.Message;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MessageRepository implements PanacheRepositoryBase<Message, UUID> {

    public Uni<List<Message>> findByCovenantId(UUID roomId, int page, int size) {
        return find("roomId = ?1 order by sentAt desc", roomId)
                .page(page, size)
                .list();
    }

    public Uni<Long> countByCovenantId(UUID roomId) {
        return count("roomId", roomId);
    }
}
