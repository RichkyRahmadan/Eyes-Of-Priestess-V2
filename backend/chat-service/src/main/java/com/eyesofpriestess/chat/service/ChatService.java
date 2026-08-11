package com.eyesofpriestess.chat.service;

import com.eyesofpriestess.chat.dto.ChatMessageDTO;
import com.eyesofpriestess.chat.entity.Message;
import com.eyesofpriestess.chat.repository.MessageRepository;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.UUID;

/**
 * ChatService — Business logic for Communion Sanctum.
 * Handles message persistence and history retrieval.
 */
@ApplicationScoped
public class ChatService {

    private static final Logger LOG = Logger.getLogger(ChatService.class);

    @Inject MessageRepository messageRepo;

    @WithTransaction
    public Uni<ChatMessageDTO> persistMessage(UUID roomId, UUID senderId, String senderDisplay, String content) {
        Message msg = new Message();
        msg.roomId = roomId;
        msg.senderId = senderId;
        msg.senderDisplay = senderDisplay;
        msg.type = Message.MessageType.USER;
        msg.content = content;

        return messageRepo.persist(msg)
                .map(ChatMessageDTO::from)
                .invoke(m -> LOG.debugf("[COMMUNION] Message persisted in Room=%s from=%s", roomId, senderDisplay));
    }

    @WithTransaction
    public Uni<ChatMessageDTO> persistSystemMessage(UUID roomId, String eventContent) {
        Message msg = new Message();
        msg.roomId = roomId;
        msg.type = Message.MessageType.SYSTEM;
        msg.content = eventContent;

        return messageRepo.persist(msg)
                .map(ChatMessageDTO::from)
                .invoke(m -> LOG.debugf("[COMMUNION] System event persisted in Room=%s: %s", roomId, eventContent));
    }

    public Uni<List<ChatMessageDTO>> getHistory(UUID roomId, int page, int size) {
        return messageRepo.findByCovenantId(roomId, page, size)
                .map(list -> list.stream().map(ChatMessageDTO::from).toList());
    }
}
