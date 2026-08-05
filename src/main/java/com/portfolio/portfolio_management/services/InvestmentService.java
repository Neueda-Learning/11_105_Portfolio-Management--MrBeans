package com.portfolio.portfolio_management.services;

import com.portfolio.portfolio_management.dto.InvestmentRequestDTO;
import com.portfolio.portfolio_management.dto.InvestmentResponseDTO;

import java.util.List;

public interface InvestmentService {
    List<InvestmentResponseDTO> getAllInvestments();

    InvestmentResponseDTO getInvestmentById(Long id);

    InvestmentResponseDTO createInvestment(Long portfolioId, InvestmentRequestDTO request);

    InvestmentResponseDTO updateInvestment(Long id, InvestmentRequestDTO request);

    void deleteInvestment(Long id);

}
