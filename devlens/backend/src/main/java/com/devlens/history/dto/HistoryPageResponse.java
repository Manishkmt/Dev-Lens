package com.devlens.history.dto;

import lombok.*;
import java.util.List;

/**
 * Paginated response wrapper for analysis history
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoryPageResponse {
    private List<HistoryItemDto> content;
    private long totalElements;
    private int totalPages;
    private int page;
    private int size;
}
