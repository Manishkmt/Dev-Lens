package com.devlens.analysis.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import java.util.List;

/**
 * Response DTO for Flowchart Control-Flow Graph analysis
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FlowchartAnalysisResponse {
    private String mermaid;
    private List<FlowNodeDto> nodes;
    private List<FlowEdgeDto> edges;
    private String selectedMethod;
    private List<String> methods;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FlowNodeDto {
        private String id;
        private String label;
        private String type; // terminal, decision, action, loop
        private Integer line;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class FlowEdgeDto {
        private String from;
        private String to;
        private String label;
    }
}
