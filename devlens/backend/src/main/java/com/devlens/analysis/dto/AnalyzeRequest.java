package com.devlens.analysis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * Request DTO for running static code analysis
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeRequest {

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Code is required")
    private String code;

    @Builder.Default
    private String level = "INTERMEDIATE";

    public boolean exceedsSizeLimit() {
        return code != null && code.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > 102400; // 100 KB
    }
}
