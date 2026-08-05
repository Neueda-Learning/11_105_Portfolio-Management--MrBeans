package com.portfoliomanager.service;

import com.portfoliomanager.repository.DividendRepository;
import com.portfoliomanager.model.DividendMode;

import com.portfoliomanager.util.MoneyMath;
import com.portfoliomanager.dto.dividend.SimulateDividendRequest;
import com.portfoliomanager.dto.dividend.SimulateDividendResponse;
import com.portfoliomanager.service.CostBasisCalculator;
import com.portfoliomanager.dto.pnl.CostBasisResult;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
public class DividendService {

    private final DividendRepository dividendRepository;
    private final TransactionRepository transactionRepository;

    public DividendService(DividendRepository dividendRepository, TransactionRepository transactionRepository) {
        this.dividendRepository = dividendRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Simulates the outcome of a dividend without saving it to the database.
     * Evaluates total current shares, computes the gross payout, and determines 
     * either the new shares acquired (ACCUMULATIVE) or the cash payout (DISTRIBUTIVE).
     */
    public SimulateDividendResponse simulateDividend(UUID investmentId, SimulateDividendRequest request) {
        List<Transaction> transactions = transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId);
        CostBasisResult costBasis = CostBasisCalculator.calculate(transactions);
        BigDecimal currentShares = costBasis.totalQuantity();

        BigDecimal totalPayout = currentShares.multiply(request.getDividendPerShare());
        
        SimulateDividendResponse response = new SimulateDividendResponse();
        response.setTotalShares(MoneyMath.roundQuantity(currentShares));
        response.setTotalDividendAmount(MoneyMath.roundCurrency(totalPayout));

        if (request.getMode() == DividendMode.ACCUMULATIVE) {
            if (request.getReinvestmentPrice() == null || request.getReinvestmentPrice().compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Reinvestment price must be greater than 0 for accumulative dividends");
            }
            BigDecimal newShares = totalPayout.divide(request.getReinvestmentPrice(), 8, RoundingMode.HALF_UP);
            response.setNewSharesAcquired(newShares);
            response.setCashPayout(BigDecimal.ZERO);
        } else {
            // DISTRIBUTIVE
            response.setCashPayout(MoneyMath.roundCurrency(totalPayout));
            response.setNewSharesAcquired(BigDecimal.ZERO);
        }

        return response;
    }
}
