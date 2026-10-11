package com.devlens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for Code Quality Review analysis
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReviewAnalysisResponse {
    private int overallScore;
    private Map<String, QualityCategoryDto> breakdown;
    private List<RecommendationDto> recommendations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class QualityCategoryDto {
        private int score;
        private String note;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RecommendationDto {
        private String priority; // HIGH, MEDIUM, LOW
        private int line;
        private String title;
        private String description;
    }
}
