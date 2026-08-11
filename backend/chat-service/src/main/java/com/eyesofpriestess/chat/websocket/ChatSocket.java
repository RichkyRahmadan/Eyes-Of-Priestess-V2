package com.eyesofpriestess.chat.websocket;

import com.eyesofpriestess.chat.dto.ChatMessageDTO;
import com.eyesofpriestess.chat.service.ChatService;
import com.eyesofpriestess.chat.service.JwtValidator;
import com.eyesofpriestess.chat.service.RoomRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.vertx.VertxContextSupport;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

import java.util.UUID;

/**
 * ChatSocket — WebSocket endpoint for real-time Room room chat.
 *
 * URL:     ws://host/ws/communion/{roomId}?token=<JWT>
 *
 * Auth:    JWT passed as query param `token` on connection.
 *          If invalid → connection closed immediately (1008 Policy Violation).
 *
 * Message: JSON payload { "content": "hello" }
 * Receive: JSON payload { id, roomId, senderId, senderDisplay, type, content, sentAt }
 *
 * System messages (status changes) are broadcast from RoomService via RoomRegistry.
 */
@ServerEndpoint("/api/v1/chat/ws/{roomId}")
@ApplicationScoped
public class ChatSocket {

    private static final Logger LOG = Logger.getLogger(ChatSocket.class);

    @Inject RoomRegistry registry;
    @Inject ChatService ChatService;
    @Inject JwtValidator jwtValidator;
    @Inject ObjectMapper objectMapper;

    @OnOpen
    public void onOpen(Session session, @PathParam("roomId") String roomId) {
        String token = session.getRequestParameterMap()
                .getOrDefault("token", java.util.List.of(""))
                .stream().findFirst().orElse("");

        boolean valid = jwtValidator.stampSession(session, token);
        if (!valid) {
            LOG.warnf("[COMMUNION] Rejected unauthenticated connection to room=%s sid=%s", roomId, session.getId());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized: valid JWT required"));
            } catch (Exception e) {
                LOG.error("[COMMUNION] Error closing unauthenticated session", e);
            }
            return;
        }

        registry.join(roomId, session);
        String display = registry.getSessionDisplay(session);
        LOG.infof("[COMMUNION] %s joined room=%s", display, roomId);

        // Broadcast join event to room
        ChatMessageDTO joinMsg = ChatMessageDTO.system(UUID.fromString(roomId),
                display + " entered the Room Chamber.");
        registry.broadcast(roomId, joinMsg);
    }

    @OnClose
    public void onClose(Session session, @PathParam("roomId") String roomId) {
        String display = registry.getSessionDisplay(session);
        registry.leave(roomId, session);
        LOG.infof("[COMMUNION] %s left room=%s", display, roomId);

        ChatMessageDTO leaveMsg = ChatMessageDTO.system(UUID.fromString(roomId),
                display + " left the Room Chamber.");
        registry.broadcast(roomId, leaveMsg);
    }

    @OnError
    public void onError(Session session, @PathParam("roomId") String roomId, Throwable error) {
        LOG.errorf(error, "[COMMUNION] Error in room=%s sid=%s", roomId, session.getId());
        registry.leave(roomId, session);
    }

    @OnMessage
    public void onMessage(String rawMessage, @PathParam("roomId") String roomId, Session session) {
        UUID userId = registry.getSessionPilgrimId(session);
        String display = registry.getSessionDisplay(session);

        if (userId == null) {
            LOG.warn("[COMMUNION] Received message from unauthenticated session, ignoring.");
            return;
        }

        String content;
        try {
            JsonNode node = objectMapper.readTree(rawMessage);
            content = node.path("content").asText("").trim();
            if (content.isEmpty()) return;
        } catch (Exception e) {
            LOG.warnf("[COMMUNION] Malformed message from %s: %s", display, rawMessage);
            return;
        }

        // Persist + broadcast asynchronously on Vert.x event loop
        ChatService.persistMessage(UUID.fromString(roomId), userId, display, content)
                .subscribe().with(
                        persisted -> registry.broadcast(roomId, persisted),
                        err -> LOG.errorf(err, "[COMMUNION] Failed to persist message in room=%s", roomId)
                );
    }
}
