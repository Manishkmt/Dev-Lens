package com.devlens.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Standard Success Message Response
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse {
    private Integer status;
    private String message;
}
