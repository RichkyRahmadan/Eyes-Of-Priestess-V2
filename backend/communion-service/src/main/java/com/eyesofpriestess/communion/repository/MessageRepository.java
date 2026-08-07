package com.eyesofpriestess.communion.repository;

import com.eyesofpriestess.communion.entity.Message;
import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class MessageRepository implements PanacheRepositoryBase<Message, UUID> {

    public Uni<List<Message>> findByCovenantId(UUID covenantId, int page, int size) {
        return find("covenantId = ?1 order by sentAt desc", covenantId)
                .page(page, size)
                .list();
    }

    public Uni<Long> countByCovenantId(UUID covenantId) {
        return count("covenantId", covenantId);
    }
}
