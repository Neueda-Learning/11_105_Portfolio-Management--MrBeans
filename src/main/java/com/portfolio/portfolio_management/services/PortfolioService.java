package com.portfolio.portfolio_management.services;
import com.portfolio.portfolio_management.entity.Portfolio;
//import jakarta.persistence.Entity;

import java.util.List;


public interface PortfolioService {
    List<Portfolio> getAllPortfolios();

    Portfolio getPortfolioById(Long id);

    Portfolio createPortfolio(Portfolio portfolio);

    Portfolio updatePortfolio(Long id, Portfolio portfolio);

    void deletePortfolio(Long id);
}
