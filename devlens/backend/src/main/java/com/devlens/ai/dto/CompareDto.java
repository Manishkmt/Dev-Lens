package com.devlens.ai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.List;
import java.util.Map;

/**
 * Request and Response DTOs for Differential Code Comparison & Algorithmic Benchmarks
 */
public class CompareDto {

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @Builder.Default
        private String language = "java";

        @NotBlank(message = "Variant A code is required")
        private String codeA;

        @NotBlank(message = "Variant B code is required")
        private String codeB;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Response {
        private List<Map<String, Object>> metrics;
        private Map<String, Object> verdict;

        @Builder.Default
        private boolean aiGenerated = true;
    }
}
