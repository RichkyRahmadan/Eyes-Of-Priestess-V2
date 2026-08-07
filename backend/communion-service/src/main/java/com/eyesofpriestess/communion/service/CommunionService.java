package com.eyesofpriestess.communion.service;

import com.eyesofpriestess.communion.dto.CommunionMessage;
import com.eyesofpriestess.communion.entity.Message;
import com.eyesofpriestess.communion.repository.MessageRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

/**
 * CommunionService — Business logic for Communion Sanctum.
 * Handles message persistence and history retrieval.
 */
@ApplicationScoped
public class CommunionService {

    private static final Logger LOG = Logger.getLogger(CommunionService.class);

    @Inject MessageRepository messageRepo;

    @WithTransaction
    public Uni<CommunionMessage> persistMessage(UUID covenantId, UUID senderId, String senderDisplay, String content) {
        Message msg = new Message();
        msg.covenantId = covenantId;
        msg.senderId = senderId;
        msg.senderDisplay = senderDisplay;
        msg.type = Message.MessageType.USER;
        msg.content = content;

        return messageRepo.persist(msg)
                .map(CommunionMessage::from)
                .invoke(m -> LOG.debugf("[COMMUNION] Message persisted in covenant=%s from=%s", covenantId, senderDisplay));
    }

    @WithTransaction
    public Uni<CommunionMessage> persistSystemMessage(UUID covenantId, String eventContent) {
        Message msg = new Message();
        msg.covenantId = covenantId;
        msg.type = Message.MessageType.SYSTEM;
        msg.content = eventContent;

        return messageRepo.persist(msg)
                .map(CommunionMessage::from)
                .invoke(m -> LOG.debugf("[COMMUNION] System event persisted in covenant=%s: %s", covenantId, eventContent));
    }

    public Uni<List<CommunionMessage>> getHistory(UUID covenantId, int page, int size) {
        return messageRepo.findByCovenantId(covenantId, page, size)
                .map(list -> list.stream().map(CommunionMessage::from).toList());
    }
}
