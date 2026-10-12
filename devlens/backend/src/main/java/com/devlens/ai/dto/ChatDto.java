package com.devlens.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Request and Response DTOs for AI Assistant Chat Conversations
 */
public class ChatDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        private String sessionId;

        @NotBlank(message = "Message text cannot be empty")
        private String message;

        @Builder.Default
        private boolean includeContext = false;

        private String code;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private String sessionId;
        private String messageId;
        private String reply;
        private String timestamp;

        @Builder.Default
        private boolean aiGenerated = true;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionSummary {
        private String id;
        private String title;
        private LocalDateTime updatedAt;
        private LocalDateTime createdAt;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MessageItem {
        private String id;
        private String sender;
        private String text;
        private String codeSnippet;
        private LocalDateTime timestamp;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SessionDetail {
        private String id;
        private String title;
        private List<MessageItem> messages;
    }
}
