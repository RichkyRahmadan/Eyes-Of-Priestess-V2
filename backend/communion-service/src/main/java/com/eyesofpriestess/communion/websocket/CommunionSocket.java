package com.eyesofpriestess.communion.websocket;

import com.eyesofpriestess.communion.dto.CommunionMessage;
import com.eyesofpriestess.communion.service.CommunionService;
import com.eyesofpriestess.communion.service.JwtValidator;
import com.eyesofpriestess.communion.service.RoomRegistry;
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
 * CommunionSocket — WebSocket endpoint for real-time Covenant Room chat.
 *
 * URL:     ws://host/ws/communion/{covenantId}?token=<JWT>
 *
 * Auth:    JWT passed as query param `token` on connection.
 *          If invalid → connection closed immediately (1008 Policy Violation).
 *
 * Message: JSON payload { "content": "hello" }
 * Receive: JSON payload { id, covenantId, senderId, senderDisplay, type, content, sentAt }
 *
 * System messages (status changes) are broadcast from CovenantService via RoomRegistry.
 */
@ServerEndpoint("/ws/communion/{covenantId}")
@ApplicationScoped
public class CommunionSocket {

    private static final Logger LOG = Logger.getLogger(CommunionSocket.class);

    @Inject RoomRegistry registry;
    @Inject CommunionService communionService;
    @Inject JwtValidator jwtValidator;
    @Inject ObjectMapper objectMapper;

    @OnOpen
    public void onOpen(Session session, @PathParam("covenantId") String covenantId) {
        String token = session.getRequestParameterMap()
                .getOrDefault("token", java.util.List.of(""))
                .stream().findFirst().orElse("");

        boolean valid = jwtValidator.stampSession(session, token);
        if (!valid) {
            LOG.warnf("[COMMUNION] Rejected unauthenticated connection to room=%s sid=%s", covenantId, session.getId());
            try {
                session.close(new CloseReason(CloseReason.CloseCodes.VIOLATED_POLICY, "Unauthorized: valid JWT required"));
            } catch (Exception e) {
                LOG.error("[COMMUNION] Error closing unauthenticated session", e);
            }
            return;
        }

        registry.join(covenantId, session);
        String display = registry.getSessionDisplay(session);
        LOG.infof("[COMMUNION] %s joined room=%s", display, covenantId);

        // Broadcast join event to room
        CommunionMessage joinMsg = CommunionMessage.system(UUID.fromString(covenantId),
                display + " entered the Covenant Chamber.");
        registry.broadcast(covenantId, joinMsg);
    }

    @OnClose
    public void onClose(Session session, @PathParam("covenantId") String covenantId) {
        String display = registry.getSessionDisplay(session);
        registry.leave(covenantId, session);
        LOG.infof("[COMMUNION] %s left room=%s", display, covenantId);

        CommunionMessage leaveMsg = CommunionMessage.system(UUID.fromString(covenantId),
                display + " left the Covenant Chamber.");
        registry.broadcast(covenantId, leaveMsg);
    }

    @OnError
    public void onError(Session session, @PathParam("covenantId") String covenantId, Throwable error) {
        LOG.errorf(error, "[COMMUNION] Error in room=%s sid=%s", covenantId, session.getId());
        registry.leave(covenantId, session);
    }

    @OnMessage
    public void onMessage(String rawMessage, @PathParam("covenantId") String covenantId, Session session) {
        UUID pilgrimId = registry.getSessionPilgrimId(session);
        String display = registry.getSessionDisplay(session);

        if (pilgrimId == null) {
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
        communionService.persistMessage(UUID.fromString(covenantId), pilgrimId, display, content)
                .subscribe().with(
                        persisted -> registry.broadcast(covenantId, persisted),
                        err -> LOG.errorf(err, "[COMMUNION] Failed to persist message in room=%s", covenantId)
                );
    }
}
