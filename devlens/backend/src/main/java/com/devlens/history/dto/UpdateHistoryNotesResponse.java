package com.devlens.history.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * Response DTO after updating analysis notes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateHistoryNotesResponse {
    private Long id;
    private String notes;
    private LocalDateTime updatedAt;
}
