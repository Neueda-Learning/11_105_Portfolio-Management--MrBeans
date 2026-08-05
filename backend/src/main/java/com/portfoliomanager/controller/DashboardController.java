package com.portfoliomanager.controller;

import com.portfoliomanager.service.DashboardService;

import com.portfoliomanager.dto.dashboard.AllocationResponse;
import com.portfoliomanager.dto.dashboard.DashboardSummaryResponse;
import com.portfoliomanager.dto.dashboard.TrendResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(@RequestParam(defaultValue = "INR") String homeCurrency) {
        // Fallback to INR for MVP, though a fully robust solution would fetch this from UserSettingsService.
        return dashboardService.getSummary(homeCurrency);
    }

    @GetMapping("/allocation")
    public List<AllocationResponse> getAllocation(@RequestParam(defaultValue = "INR") String homeCurrency) {
        return dashboardService.getAllocation(homeCurrency);
    }

    @GetMapping("/trend")
    public List<TrendResponse> getTrend(
            @RequestParam(defaultValue = "INR") String homeCurrency,
            @RequestParam(defaultValue = "30") int days) {
        return dashboardService.getTrend(homeCurrency, days);
    }
}
