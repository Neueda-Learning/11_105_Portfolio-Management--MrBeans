package com.portfolio.portfolio_management.controller;

import com.portfolio.portfolio_management.entity.Portfolio;
import com.portfolio.portfolio_management.services.PortfolioService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {
    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    // GET ALL PORTFOLIOS
    @GetMapping
    public List<Portfolio> getAllPortfolios() {
        return portfolioService.getAllPortfolios();
    }

    // GET PORTFOLIO BY ID
    @GetMapping("/{id}")
    public Portfolio getPortfolioById(@PathVariable Long id) {
        return portfolioService.getPortfolioById(id);
    }

    // CREATE PORTFOLIO
    @PostMapping
    public Portfolio createPortfolio(@RequestBody Portfolio portfolio) {
        return portfolioService.createPortfolio(portfolio);
    }

    // UPDATE PORTFOLIO
    @PutMapping("/{id}")
    public Portfolio updatePortfolio(@PathVariable Long id,
                                     @RequestBody Portfolio portfolio) {
        return portfolioService.updatePortfolio(id, portfolio);
    }

    // DELETE PORTFOLIO
    @DeleteMapping("/{id}")
    public void deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
    }



}
