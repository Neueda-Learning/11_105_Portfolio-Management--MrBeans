package com.portfoliomanager.controller;

import com.portfoliomanager.service.InvestmentService;

import com.portfoliomanager.dto.investment.CreateInvestmentRequest;
import com.portfoliomanager.dto.investment.UpdateInvestmentRequest;
import com.portfoliomanager.dto.investment.InvestmentResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {

    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteInvestment(@PathVariable UUID id) {
        investmentService.deleteInvestment(id);
    }
}
