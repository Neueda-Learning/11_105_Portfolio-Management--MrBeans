package com.portfolio.portfolio_management.services.impl;

import com.portfolio.portfolio_management.entity.Investment;
import com.portfolio.portfolio_management.entity.Portfolio;
import com.portfolio.portfolio_management.repository.InvestmentRepository;
import com.portfolio.portfolio_management.repository.PortfolioRepository;
import com.portfolio.portfolio_management.services.InvestmentService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class InvestmentServiceImpl implements InvestmentService {
    private final InvestmentRepository investmentRepository;
    private final PortfolioRepository portfolioRepository;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository,
                                 PortfolioRepository portfolioRepository) {

        this.investmentRepository = investmentRepository;
        this.portfolioRepository = portfolioRepository;
    }


    @Override
    public List<Investment> getAllInvestments() {
        return investmentRepository.findAll();
    }

    @Override
    public Investment getInvestmentById(Long id) {
        return investmentRepository.findById(id).orElse(null);
    }

    @Override
    public Investment createInvestment(Long portfolioId, Investment investment) {

        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);

        if (portfolio != null) {
            investment.setPortfolio(portfolio);
            return investmentRepository.save(investment);
        }

        return null;
    }

    @Override
    public Investment updateInvestment(Long id, Investment investment) {

        Investment existingInvestment =
                investmentRepository.findById(id).orElse(null);

        if (existingInvestment != null) {

            existingInvestment.setAssetName(investment.getAssetName());
            existingInvestment.setTickerSymbol(investment.getTickerSymbol());
            existingInvestment.setAssetType(investment.getAssetType());
            existingInvestment.setQuantity(investment.getQuantity());
            existingInvestment.setPurchasePrice(investment.getPurchasePrice());
            existingInvestment.setCurrentPrice(investment.getCurrentPrice());
            existingInvestment.setPurchaseDate(investment.getPurchaseDate());
            existingInvestment.setCurrency(investment.getCurrency());

            return investmentRepository.save(existingInvestment);
        }

        return null;
    }

    @Override
    public void deleteInvestment(Long id) {
        investmentRepository.deleteById(id);
    }
}
