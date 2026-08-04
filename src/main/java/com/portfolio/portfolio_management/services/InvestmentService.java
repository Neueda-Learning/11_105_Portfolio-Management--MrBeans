package com.portfolio.portfolio_management.services;

import com.portfolio.portfolio_management.entity.Investment;


import java.util.List;

public interface InvestmentService {
    List<Investment> getAllInvestments();

    Investment getInvestmentById(Long id);

    Investment createInvestment(Long portfolioId, Investment investment);

    Investment updateInvestment(Long id, Investment investment);

    void deleteInvestment(Long id);

}
