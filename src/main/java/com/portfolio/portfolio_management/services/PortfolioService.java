package com.portfolio.portfolio_management.services;

import com.portfolio.portfolio_management.dto.PortfolioRequestDTO;
import com.portfolio.portfolio_management.dto.PortfolioResponseDTO;

import java.util.List;


public interface PortfolioService {
    List<PortfolioResponseDTO> getAllPortfolios();

    PortfolioResponseDTO getPortfolioById(Long id);

    PortfolioResponseDTO createPortfolio(PortfolioRequestDTO request);

    PortfolioResponseDTO updatePortfolio(Long id, PortfolioRequestDTO request);

    void deletePortfolio(Long id);
}
