package com.portfoliomanager.controller;

import com.portfoliomanager.service.DashboardService;

import com.portfoliomanager.dto.dashboard.AllocationResponse;
import com.portfoliomanager.dto.dashboard.DashboardSummaryResponse;
import com.portfoliomanager.dto.dashboard.PerformanceResponse;
import com.portfoliomanager.dto.dashboard.TrendResponse;
import com.portfoliomanager.model.InvestmentType;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@Validated
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(
            @RequestParam(defaultValue = "INR")
            @Pattern(regexp = "^[A-Za-z]{3}$", message = "homeCurrency must be a 3-letter ISO code")
            String homeCurrency) {
        // Fallback to INR for MVP, though a fully robust solution would fetch this from UserSettingsService.
        return dashboardService.getSummary(homeCurrency);
    }

    @GetMapping("/allocation")
    public List<AllocationResponse> getAllocation(
            @RequestParam(defaultValue = "INR")
            @Pattern(regexp = "^[A-Za-z]{3}$", message = "homeCurrency must be a 3-letter ISO code")
            String homeCurrency) {
        return dashboardService.getAllocation(homeCurrency);
    }

    @GetMapping("/trend")
    public List<TrendResponse> getTrend(
            @RequestParam(defaultValue = "INR")
            @Pattern(regexp = "^[A-Za-z]{3}$", message = "homeCurrency must be a 3-letter ISO code")
            String homeCurrency,
            @RequestParam(defaultValue = "30")
            @Min(value = 1, message = "days must be at least 1")
            @Max(value = 3650, message = "days must be 3650 or less")
            int days) {
        return dashboardService.getTrend(homeCurrency, days);
    }

    @GetMapping("/trend/filter")
    public List<TrendResponse> getTrendFiltered(
            @RequestParam(defaultValue = "INR")
            @Pattern(regexp = "^[A-Za-z]{3}$", message = "homeCurrency must be a 3-letter ISO code")
            String homeCurrency,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) List<InvestmentType> types,
            @RequestParam(defaultValue = "30")
            @Min(value = 1, message = "days must be at least 1")
            @Max(value = 3650, message = "days must be 3650 or less")
            int days) {
        return dashboardService.getTrendFiltered(homeCurrency, fromDate, toDate, types, days);
    }

    @GetMapping("/performance")
    public List<PerformanceResponse> getPerformance(@RequestParam(defaultValue = "INR") String homeCurrency) {
        return dashboardService.getPerformance(homeCurrency);
    }
}
