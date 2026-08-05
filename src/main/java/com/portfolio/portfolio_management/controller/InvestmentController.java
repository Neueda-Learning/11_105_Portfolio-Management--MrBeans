package com.portfolio.portfolio_management.controller;

import com.portfolio.portfolio_management.dto.InvestmentRequestDTO;
import com.portfolio.portfolio_management.dto.InvestmentResponseDTO;
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
    public List<InvestmentResponseDTO> getAllInvestments() {
        return investmentService.getAllInvestments();
    }

    // GET INVESTMENT BY ID
    @GetMapping("/{id}")
    public InvestmentResponseDTO getInvestmentById(@PathVariable Long id) {
        return investmentService.getInvestmentById(id);
    }

    // CREATE INVESTMENT
    @PostMapping("/portfolio/{portfolioId}")
    public InvestmentResponseDTO createInvestment(@PathVariable Long portfolioId,
                                                  @RequestBody InvestmentRequestDTO request) {
        return investmentService.createInvestment(portfolioId, request);
    }

    // UPDATE INVESTMENT
    @PutMapping("/{id}")
    public InvestmentResponseDTO updateInvestment(@PathVariable Long id,
                                                  @RequestBody InvestmentRequestDTO request) {
        return investmentService.updateInvestment(id, request);
    }

    // DELETE INVESTMENT
    @DeleteMapping("/{id}")
    public void deleteInvestment(@PathVariable Long id) {
        investmentService.deleteInvestment(id);
    }
}
