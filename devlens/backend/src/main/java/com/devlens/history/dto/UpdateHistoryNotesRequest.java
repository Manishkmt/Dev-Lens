package com.devlens.history.dto;

import lombok.*;

/**
 * Request DTO for updating analysis notes
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateHistoryNotesRequest {
    private String notes;
}
