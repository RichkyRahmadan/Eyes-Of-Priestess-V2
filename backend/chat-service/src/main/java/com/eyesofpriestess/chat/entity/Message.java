package com.eyesofpriestess.chat.entity;

import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Message — A persisted Communion chat message inside a Room room.
 *
 * Types:
 * - USER: normal User message
 * - SYSTEM: auto-generated status transition notification (e.g. "Room SEALED")
 */
@Entity
@Table(name = "messages", schema = "chat")
public class Message extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    public UUID id;

    @Column(name = "covenant_id", nullable = false)
    public UUID roomId;

    @Column(name = "sender_id")
    public UUID senderId;

    @Column(name = "sender_display", length = 100)
    public String senderDisplay;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    public MessageType type = MessageType.USER;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    public String content;

    @Column(name = "is_read", nullable = false)
    public boolean isRead = false;

    @Column(name = "sent_at", nullable = false, updatable = false)
    public Instant sentAt = Instant.now();

    public enum MessageType {
        USER,   // Sent by a User
        SYSTEM  // Auto-generated status event notification
    }
}
