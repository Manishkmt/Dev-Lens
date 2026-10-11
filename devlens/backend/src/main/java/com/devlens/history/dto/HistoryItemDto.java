package com.devlens.history.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.time.LocalDateTime;

/**
 * Summary DTO for paginated history listing
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HistoryItemDto {
    private Long id;
    private String title;
    private String projectName;
    private String language;
    private String type;
    private Integer qualityScore;
    private Integer issuesCount;
    private String notes;
    private LocalDateTime createdAt;
}
