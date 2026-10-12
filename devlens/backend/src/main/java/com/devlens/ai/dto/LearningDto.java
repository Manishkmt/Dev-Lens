package com.devlens.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Request and Response DTOs for AI Learning Walkthrough, Quiz & Viva Questions
 */
public class LearningDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @Builder.Default
        private String topic = "Bubble Sort";

        @Builder.Default
        private String language = "java";

        @Builder.Default
        private String level = "BEGINNER";

        private String code;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private Map<String, Object> concept;
        private List<Map<String, Object>> steps;
        private List<Map<String, Object>> quiz;
        private List<Map<String, Object>> vivaQuestions;
        private List<String> relatedTopics;

        @Builder.Default
        private boolean aiGenerated = true;
    }
}
