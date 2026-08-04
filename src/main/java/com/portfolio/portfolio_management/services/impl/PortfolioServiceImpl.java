package com.portfolio.portfolio_management.services.impl;

import com.portfolio.portfolio_management.entity.Portfolio;
import com.portfolio.portfolio_management.repository.PortfolioRepository;
import com.portfolio.portfolio_management.services.PortfolioService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PortfolioServiceImpl implements PortfolioService {
    private final PortfolioRepository repository;
    public PortfolioServiceImpl(PortfolioRepository repository){
        this.repository = repository;
    }

    @Override
    public List<Portfolio> getAllPortfolios() {
        return repository.findAll();
    }

    @Override
    public Portfolio getPortfolioById(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public Portfolio createPortfolio(Portfolio portfolio) {
        return repository.save(portfolio);
    }

    @Override
    public Portfolio updatePortfolio(Long id, Portfolio portfolio) {
        Portfolio existingPortfolio = repository.findById(id).orElse(null);

        if(existingPortfolio != null){
            existingPortfolio.setPortfolioName(portfolio.getPortfolioName());
            return repository.save(existingPortfolio);
        }

        return null;
    }

    @Override
    public void deletePortfolio(Long id) {
        repository.deleteById(id);
    }
}
