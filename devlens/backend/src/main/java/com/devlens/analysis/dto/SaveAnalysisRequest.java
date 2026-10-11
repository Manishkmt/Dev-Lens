package com.devlens.analysis.dto;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for saving analysis results
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaveAnalysisRequest {

    @NotBlank(message = "Analysis type is required")
    private String type;

    @NotBlank(message = "Programming language is required")
    private String language;

    private String code;

    /**
     * Accepts either a raw JSON string or structured JSON object node
     */
    private JsonNode resultJson;

    private Integer qualityScore;

    private Long projectId;

    private Long fileId;

    private String notes;
}
