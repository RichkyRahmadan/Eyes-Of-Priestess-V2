package com.eyesofpriestess.chat.dto;

import com.eyesofpriestess.chat.entity.Message;
import java.time.Instant;
import java.util.UUID;

/**
 * ChatMessageDTO — wire format for WebSocket frames and REST history responses.
 *
 * On send:  { "content": "hello" }
 * On recv:  { "id": "...", "senderId": "...", "senderDisplay": "...", "type": "USER", "content": "...", "sentAt": "..." }
 */
public class ChatMessageDTO {
    public UUID id;
    public UUID roomId;
    public UUID senderId;
    public String senderDisplay;
    public String type;      // USER | SYSTEM
    public String content;
    public boolean isRead;
    public Instant sentAt;

    public ChatMessageDTO() {}

    public static ChatMessageDTO from(Message m) {
        ChatMessageDTO r = new ChatMessageDTO();
        r.id = m.id;
        r.roomId = m.roomId;
        r.senderId = m.senderId;
        r.senderDisplay = m.senderDisplay;
        r.type = m.type.name();
        r.content = m.content;
        r.isRead = m.isRead;
        r.sentAt = m.sentAt;
        return r;
    }

    /** System-generated event message (no sender) */
    public static ChatMessageDTO system(UUID roomId, String content) {
        ChatMessageDTO r = new ChatMessageDTO();
        r.roomId = roomId;
        r.type = "SYSTEM";
        r.content = content;
        r.sentAt = Instant.now();
        return r;
    }
}
