package com.devlens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.Map;

/**
 * Comprehensive Analysis Response returning all tabs combined
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComprehensiveAnalysisResponse {
    @Builder.Default
    private String status = "success";
    private String language;
    private ErrorAnalysisResponse errors;
    private ComplexityAnalysisResponse complexity;
    private ReviewAnalysisResponse review;
    private FlowchartAnalysisResponse flowchart;
    private Map<String, Object> explanation;
    private Map<String, Object> optimization;
    private Map<String, Object> docs;
}
