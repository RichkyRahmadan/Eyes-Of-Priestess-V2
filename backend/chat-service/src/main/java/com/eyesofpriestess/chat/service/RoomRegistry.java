package com.eyesofpriestess.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.websocket.Session;
import org.jboss.logging.Logger;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * RoomRegistry — In-memory registry of active WebSocket sessions grouped by covenant room.
 *
 * Each covenant room has a Set of open WebSocket Sessions.
 * On message, the payload is broadcast to all members of that room.
 */
@ApplicationScoped
public class RoomRegistry {

    private static final Logger LOG = Logger.getLogger(RoomRegistry.class);

    /** Map: covenantId → Set of active WebSocket sessions */
    private final Map<String, Set<Session>> rooms = new ConcurrentHashMap<>();

    @Inject ObjectMapper objectMapper;

    public void join(String covenantId, Session session) {
        rooms.computeIfAbsent(covenantId, k -> ConcurrentHashMap.newKeySet()).add(session);
        LOG.infof("[COMMUNION-REGISTRY] Session %s joined room %s. Active in room: %d",
                session.getId(), covenantId, rooms.get(covenantId).size());
    }

    public void leave(String covenantId, Session session) {
        Set<Session> sessions = rooms.get(covenantId);
        if (sessions != null) {
            sessions.remove(session);
            if (sessions.isEmpty()) rooms.remove(covenantId);
        }
        LOG.infof("[COMMUNION-REGISTRY] Session %s left room %s", session.getId(), covenantId);
    }

    public void broadcast(String covenantId, Object payload) {
        Set<Session> sessions = rooms.getOrDefault(covenantId, Set.of());
        if (sessions.isEmpty()) return;

        String json;
        try {
            json = objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            LOG.errorf(e, "[COMMUNION-REGISTRY] Failed to serialize broadcast payload");
            return;
        }

        // Only send to open sessions
        Set<Session> openSessions = sessions.stream()
                .filter(Session::isOpen)
                .collect(Collectors.toSet());

        for (Session s : openSessions) {
            s.getAsyncRemote().sendText(json, result -> {
                if (!result.isOK()) {
                    LOG.warnf("[COMMUNION-REGISTRY] Failed to send to session %s: %s",
                            s.getId(), result.getException().getMessage());
                }
            });
        }
    }

    public int getActiveSessionCount(String covenantId) {
        Set<Session> sessions = rooms.get(covenantId);
        return sessions == null ? 0 : sessions.size();
    }

    public int getTotalSessions() {
        return rooms.values().stream().mapToInt(Set::size).sum();
    }

    public UUID getSessionPilgrimId(Session session) {
        return (UUID) session.getUserProperties().get("pilgrimId");
    }

    public String getSessionDisplay(Session session) {
        return (String) session.getUserProperties().getOrDefault("display", "Unknown Pilgrim");
    }
}
