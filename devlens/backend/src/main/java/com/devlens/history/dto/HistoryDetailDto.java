package com.devlens.history.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Detailed DTO for viewing full analysis run with result payload
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryDetailDto {
    private Long id;
    private String title;
    private Long projectId;
    private String projectName;
    private String language;
    private String type;
    private String code;
    private JsonNode resultJson;
    private Integer qualityScore;
    private String notes;
    private LocalDateTime createdAt;
}
