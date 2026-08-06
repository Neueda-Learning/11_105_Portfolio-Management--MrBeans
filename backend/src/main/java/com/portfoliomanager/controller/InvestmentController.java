package com.portfoliomanager.controller;

import com.portfoliomanager.service.InvestmentService;
import com.portfoliomanager.service.DashboardService;
import com.portfoliomanager.dto.investment.CreateInvestmentRequest;
import com.portfoliomanager.dto.investment.UpdateInvestmentRequest;
import com.portfoliomanager.dto.investment.InvestmentResponse;
import com.portfoliomanager.dto.pnl.InvestmentPnlResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

    private final InvestmentService investmentService;
    private final DashboardService dashboardService;

    public InvestmentController(InvestmentService investmentService, DashboardService dashboardService) {
        this.investmentService = investmentService;
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public List<InvestmentResponse> getAllInvestments() {
        return investmentService.getAllInvestments();
    }

    @GetMapping("/{id}")
    public InvestmentResponse getInvestmentById(@PathVariable UUID id) {
        return investmentService.getInvestmentById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InvestmentResponse createInvestment(@Valid @RequestBody CreateInvestmentRequest request) {
        return investmentService.createInvestment(request);
    }

    @PutMapping("/{id}")
    public InvestmentResponse updateInvestment(@PathVariable UUID id, @Valid @RequestBody UpdateInvestmentRequest request) {
        return investmentService.updateInvestment(id, request);
    }

    @GetMapping("/{id}/pnl")
    public InvestmentPnlResponse getInvestmentPnl(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "INR") String homeCurrency) {
        return dashboardService.getInvestmentPnl(id, homeCurrency);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvestment(@PathVariable UUID id) {
        investmentService.deleteInvestment(id);
    }
}
