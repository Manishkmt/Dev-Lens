package com.devlens.dashboard.dto;

import lombok.*;
import java.util.List;

/**
 * DTO representing top summary metrics on the developer dashboard
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDto {
    private long totalAnalyses;
    private String analysesGrowth;
    private int bugsDetectedAndFixed;
    private int highSeverityResolved;
    private int avgQualityScore;
    private String qualityTier;
    private long activeProjects;
    private List<String> languages;
}
