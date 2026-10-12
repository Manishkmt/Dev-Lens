package com.devlens.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Request and Response DTOs for AI Debugging and Fault Localization
 */
public class DebugDto {

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
        @Builder.Default
        private String status = "success";
        private int issuesCount;
        private int warnings;
        private int infos;
        private List<Map<String, Object>> issues;

        @Builder.Default
        private boolean aiGenerated = true;
    }
}
