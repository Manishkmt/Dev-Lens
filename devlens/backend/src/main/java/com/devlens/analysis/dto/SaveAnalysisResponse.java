package com.devlens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO after saving analysis result
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SaveAnalysisResponse {
    private Long id;
    private String type;
    private String language;
    private Integer qualityScore;
    private LocalDateTime createdAt;
}
