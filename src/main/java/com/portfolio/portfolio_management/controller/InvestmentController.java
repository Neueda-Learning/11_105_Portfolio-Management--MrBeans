package com.portfolio.portfolio_management.controller;

import com.portfolio.portfolio_management.entity.Investment;
import com.portfolio.portfolio_management.services.InvestmentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/investments")
public class InvestmentController {
    private final InvestmentService investmentService;

    public InvestmentController(InvestmentService investmentService) {
        this.investmentService = investmentService;
    }

    // GET ALL INVESTMENTS
    @GetMapping
    public List<Investment> getAllInvestments() {
        return investmentService.getAllInvestments();
    }

    // GET INVESTMENT BY ID
    @GetMapping("/{id}")
    public Investment getInvestmentById(@PathVariable Long id) {
        return investmentService.getInvestmentById(id);
    }

    // CREATE INVESTMENT
    @PostMapping("/portfolio/{portfolioId}")
    public Investment createInvestment(@PathVariable Long portfolioId,
                                       @RequestBody Investment investment) {
        return investmentService.createInvestment(portfolioId, investment);
    }

    // UPDATE INVESTMENT
    @PutMapping("/{id}")
    public Investment updateInvestment(@PathVariable Long id,
                                       @RequestBody Investment investment) {
        return investmentService.updateInvestment(id, investment);
    }

    // DELETE INVESTMENT
    @DeleteMapping("/{id}")
    public void deleteInvestment(@PathVariable Long id) {
        investmentService.deleteInvestment(id);
    }
}
