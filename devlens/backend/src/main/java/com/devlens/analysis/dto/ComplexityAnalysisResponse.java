package com.devlens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;
import java.util.Map;

/**
 * Response DTO for Algorithmic Complexity analysis
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplexityAnalysisResponse {
    private Map<String, String> timeComplexity;
    private Map<String, Object> spaceComplexity;
    private String explanation;
    private List<String> reasoningSteps;
    private Map<String, String> loopMultiplier;
}
