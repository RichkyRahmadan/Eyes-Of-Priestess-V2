package com.eyesofpriestess.chat.dto;

import com.eyesofpriestess.chat.entity.Message;
import java.time.Instant;
import java.util.UUID;

/**
 * CommunionMessage — wire format for WebSocket frames and REST history responses.
 *
 * On send:  { "content": "hello" }
 * On recv:  { "id": "...", "senderId": "...", "senderDisplay": "...", "type": "USER", "content": "...", "sentAt": "..." }
 */
public class CommunionMessage {
    public UUID id;
    public UUID covenantId;
    public UUID senderId;
    public String senderDisplay;
    public String type;      // USER | SYSTEM
    public String content;
    public boolean isRead;
    public Instant sentAt;

    public CommunionMessage() {}

    public static CommunionMessage from(Message m) {
        CommunionMessage r = new CommunionMessage();
        r.id = m.id;
        r.covenantId = m.covenantId;
        r.senderId = m.senderId;
        r.senderDisplay = m.senderDisplay;
        r.type = m.type.name();
        r.content = m.content;
        r.isRead = m.isRead;
        r.sentAt = m.sentAt;
        return r;
    }

    /** System-generated event message (no sender) */
    public static CommunionMessage system(UUID covenantId, String content) {
        CommunionMessage r = new CommunionMessage();
        r.covenantId = covenantId;
        r.type = "SYSTEM";
        r.content = content;
        r.sentAt = Instant.now();
        return r;
    }
}
