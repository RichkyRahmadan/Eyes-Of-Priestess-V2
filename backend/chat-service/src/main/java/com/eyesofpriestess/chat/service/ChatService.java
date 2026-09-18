package com.eyesofpriestess.chat.service;

import com.eyesofpriestess.chat.dto.ChatMessageDTO;
import com.eyesofpriestess.chat.entity.Message;
import com.eyesofpriestess.chat.repository.MessageRepository;
import io.quarkus.hibernate.reactive.panache.common.WithSession;
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

import java.util.*;

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

    @WithSession
    public Uni<List<ChatMessageDTO>> getHistory(UUID roomId, int page, int size) {
        return messageRepo.findByCovenantId(roomId, page, size)
                .map(list -> list.stream().map(ChatMessageDTO::from).toList());
    }

    @WithSession
    public Uni<List<Map<String, Object>>> getActiveRooms(UUID userId) {
        return messageRepo.listAll()
                .map(messages -> {
                    Map<UUID, Map<String, Object>> roomMap = new LinkedHashMap<>();
                    for (Message m : messages) {
                        if (m.roomId == null) continue;
                        if (!roomMap.containsKey(m.roomId)) {
                            Map<String, Object> r = new HashMap<>();
                            r.put("id", m.roomId.toString());
                            r.put("roomId", m.roomId.toString());
                            r.put("title", "Escrow Communion #" + m.roomId.toString().substring(0, 8));
                            r.put("lastMessage", m.content);
                            r.put("lastMessageTime", m.sentAt);
                            r.put("unreadCount", 0);
                            roomMap.put(m.roomId, r);
                        }
                    }

                    return new ArrayList<>(roomMap.values());
                });
    }
}

