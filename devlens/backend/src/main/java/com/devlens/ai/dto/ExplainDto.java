package com.devlens.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Request and Response DTOs for AI Code Explanation
 */
public class ExplainDto {

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
        private String summary;
        private String confidence;
        private String signature;
        private String returnType;
        private String precondition;
        private String postcondition;
        private List<String> tags;
        private List<Map<String, Object>> stepByStep;
        private List<Map<String, Object>> lineByLine;
        private List<Map<String, Object>> methodExplanations;
        private List<Map<String, Object>> variableExplanations;
        private List<String> conceptChips;

        @Builder.Default
        private boolean aiGenerated = true;
    }
}
