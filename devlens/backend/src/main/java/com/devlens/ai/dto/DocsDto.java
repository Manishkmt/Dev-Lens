package com.devlens.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request and Response DTOs for AI Documentation Synthesis
 */
public class DocsDto {

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
        private String format = "JAVADOC";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private String javadoc;
        private String markdown;
        private String projectSummary;

        @Builder.Default
        private boolean aiGenerated = true;
    }
}
