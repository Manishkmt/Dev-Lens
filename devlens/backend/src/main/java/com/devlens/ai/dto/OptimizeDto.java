package com.devlens.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.Map;

/**
 * Request and Response DTOs for AI Code Optimization & Performance Refactoring
 */
public class OptimizeDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @Builder.Default
        private String language = "java";

        @NotBlank(message = "Code snippet is required")
        private String code;

        @Builder.Default
        private String level = "INTERMEDIATE";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private String jitStatus;
        private String suggestionTitle;
        private String description;
        private String refactoredSnippet;
        private Map<String, Object> tradeOffs;
        private Map<String, Object> benchmark;

        @Builder.Default
        private boolean aiGenerated = true;
    }
}
