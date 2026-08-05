package com.portfolio.portfolio_management.controller;

import com.portfolio.portfolio_management.dto.PortfolioRequestDTO;
import com.portfolio.portfolio_management.dto.PortfolioResponseDTO;
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
    public List<PortfolioResponseDTO> getAllPortfolios() {
        return portfolioService.getAllPortfolios();
    }

    // GET PORTFOLIO BY ID
    @GetMapping("/{id}")
    public PortfolioResponseDTO getPortfolioById(@PathVariable Long id) {
        return portfolioService.getPortfolioById(id);
    }

    // CREATE PORTFOLIO
    @PostMapping
    public PortfolioResponseDTO createPortfolio(@RequestBody PortfolioRequestDTO request) {
        return portfolioService.createPortfolio(request);
    }

    // UPDATE PORTFOLIO
    @PutMapping("/{id}")
    public PortfolioResponseDTO updatePortfolio(@PathVariable Long id,
                                                @RequestBody PortfolioRequestDTO request) {
        return portfolioService.updatePortfolio(id, request);
    }

    // DELETE PORTFOLIO
    @DeleteMapping("/{id}")
    public void deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
    }
}
