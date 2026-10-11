package com.devlens.dashboard.dto;

import lombok.*;
import java.util.List;

/**
 * DTO representing activity timeline and analysis category distribution
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardChartDataDto {
    private ActivityDto activity;
    private DistributionDto distribution;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ActivityDto {
        private List<String> labels;
        private List<Integer> analysesCount;
        private List<Integer> bugsFound;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DistributionDto {
        private List<String> labels;
        private List<Integer> data;
    }
}
