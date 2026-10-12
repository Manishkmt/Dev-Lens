package com.devlens.ai.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ChatMessage Entity
 * Represents an individual message in a ChatSession, sent by either 'user' or 'ai'.
 */
@Entity
@Table(name = "chat_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    @Id
    @Column(length = 64)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id", nullable = false)
    private ChatSession session;

    @Column(nullable = false, length = 16)
    private String sender; // "user" or "ai"

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String text;

    @Lob
    @Column(name = "code_snippet", columnDefinition = "LONGTEXT")
    private String codeSnippet;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
