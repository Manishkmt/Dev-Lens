package com.devlens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Response DTO for Error & Debugging static analysis
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorAnalysisResponse {
    @Builder.Default
    private String status = "success";
    private int issuesCount;
    private int warnings;
    private int infos;
    private int errors;
    @Builder.Default
    private List<IssueDto> issues = new ArrayList<>();
}
