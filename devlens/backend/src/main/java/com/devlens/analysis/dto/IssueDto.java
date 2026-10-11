package com.devlens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

/**
 * Representation of a detected code issue or hazard
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class IssueDto {
    private String id;
    private String type;         // ERROR, WARNING, INFO
    private String severity;     // ERROR, WARNING, INFO
    private String severityClass; // badge-danger, badge-warning, badge-info
    private String title;
    private int line;
    private int endLine;
    private String codeSnippet;
    private String explanation;
    private String whyItHappens;
    private String suggestedFix;
    private String fixCode;
}
