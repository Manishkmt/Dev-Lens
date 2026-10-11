package com.devlens.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.Map;

/**
 * Standard Error Response Payload
 * Strictly adheres to frontend/docs/API_CONTRACT.md format:
 * { "status": 400, "error": "...", "fields": { ... } }
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private int status;
    private String error;
    private Map<String, String> fields;
}
