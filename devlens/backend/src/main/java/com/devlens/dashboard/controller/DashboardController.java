package com.devlens.dashboard.controller;

import com.devlens.common.config.UserPrincipal;
import com.devlens.dashboard.dto.DashboardChartDataDto;
import com.devlens.dashboard.dto.DashboardStatsDto;
import com.devlens.dashboard.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for Dashboard metrics and charts
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats(@AuthenticationPrincipal UserPrincipal principal) {
        DashboardStatsDto stats = dashboardService.getStats(principal.getId());
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/chart-data")
    public ResponseEntity<DashboardChartDataDto> getChartData(@AuthenticationPrincipal UserPrincipal principal) {
        DashboardChartDataDto chartData = dashboardService.getChartData(principal.getId());
        return ResponseEntity.ok(chartData);
    }
}
