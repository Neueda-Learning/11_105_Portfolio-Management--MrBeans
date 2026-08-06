package com.portfoliomanager.service;

import com.portfoliomanager.repository.DividendRepository;
import com.portfoliomanager.model.Dividend;
import com.portfoliomanager.model.DividendMode;
import com.portfoliomanager.model.Transaction;

import com.portfoliomanager.util.MoneyMath;
import com.portfoliomanager.dto.dividend.CreateDividendRequest;
import com.portfoliomanager.dto.dividend.DividendResponse;
import com.portfoliomanager.dto.dividend.SimulateDividendRequest;
import com.portfoliomanager.dto.dividend.SimulateDividendResponse;
import com.portfoliomanager.dto.pnl.CostBasisResult;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DividendService {

    private final DividendRepository dividendRepository;
    private final TransactionRepository transactionRepository;
    private final InvestmentRepository investmentRepository;

    public DividendService(DividendRepository dividendRepository,
                           TransactionRepository transactionRepository,
                           InvestmentRepository investmentRepository) {
        this.dividendRepository = dividendRepository;
        this.transactionRepository = transactionRepository;
        this.investmentRepository = investmentRepository;
    }

    @Transactional(readOnly = true)
    public List<DividendResponse> getDividendsByInvestment(UUID investmentId) {
        if (!investmentRepository.existsById(investmentId)) {
            throw new ResourceNotFoundException("Investment not found with id " + investmentId);
        }
        return dividendRepository.findByInvestmentIdOrderByPaymentDateDesc(investmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DividendResponse createDividend(UUID investmentId, CreateDividendRequest request) {
        if (!investmentRepository.existsById(investmentId)) {
            throw new ResourceNotFoundException("Investment not found with id " + investmentId);
        }

        BigDecimal withholdingTax = request.getWithholdingTax() != null
                ? MoneyMath.roundCurrency(request.getWithholdingTax())
                : BigDecimal.ZERO;

        Dividend dividend = new Dividend();
        dividend.setInvestmentId(investmentId);
        dividend.setAmount(MoneyMath.roundCurrency(request.getAmount()));
        dividend.setDividendPerShare(request.getDividendPerShare() != null
                ? request.getDividendPerShare() : null);
        dividend.setCurrency(request.getCurrency().trim().toUpperCase());
        dividend.setWithholdingTax(withholdingTax);
        dividend.setMode(request.getMode());
        dividend.setExDate(request.getExDate());
        dividend.setPaymentDate(request.getPaymentDate());

        // Store reinvestment price for ACCUMULATIVE dividends
        if (request.getMode() == DividendMode.ACCUMULATIVE && request.getReinvestmentPrice() != null) {
            dividend.setReinvestmentPrice(MoneyMath.roundRate(request.getReinvestmentPrice()));
        }

        Dividend saved = dividendRepository.save(dividend);

        // Auto-create BUY transaction for ACCUMULATIVE dividends when reinvestment price is provided
        if (request.getMode() == DividendMode.ACCUMULATIVE
                && request.getReinvestmentPrice() != null
                && request.getReinvestmentPrice().compareTo(BigDecimal.ZERO) > 0) {

            BigDecimal netAmount = request.getAmount().subtract(withholdingTax);
            BigDecimal newShares = netAmount.divide(request.getReinvestmentPrice(), 8, RoundingMode.HALF_UP);

            Transaction buyTxn = new Transaction();
            buyTxn.setInvestmentId(investmentId);
            buyTxn.setType(com.portfoliomanager.model.TransactionType.BUY);
            buyTxn.setQuantity(MoneyMath.roundQuantity(newShares));
            buyTxn.setPrice(MoneyMath.roundRate(request.getReinvestmentPrice()));
            buyTxn.setCurrency(request.getCurrency().trim().toUpperCase());
            buyTxn.setTxnDate(request.getPaymentDate());
            transactionRepository.save(buyTxn);
        }

        return mapToResponse(saved);
    }

    @Transactional
    public void deleteDividend(UUID investmentId, UUID dividendId) {
        Dividend dividend = dividendRepository.findById(dividendId)
                .orElseThrow(() -> new ResourceNotFoundException("Dividend not found with id " + dividendId));
        if (!dividend.getInvestmentId().equals(investmentId)) {
            throw new ResourceNotFoundException("Dividend not found for this investment");
        }
        dividendRepository.delete(dividend);
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

    private DividendResponse mapToResponse(Dividend dividend) {
        DividendResponse response = new DividendResponse();
        response.setId(dividend.getId());
        response.setInvestmentId(dividend.getInvestmentId());
        response.setAmount(dividend.getAmount());
        response.setDividendPerShare(dividend.getDividendPerShare());
        response.setCurrency(dividend.getCurrency());
        response.setWithholdingTax(dividend.getWithholdingTax());
        response.setReinvestmentPrice(dividend.getReinvestmentPrice());
        response.setMode(dividend.getMode());
        response.setExDate(dividend.getExDate());
        response.setPaymentDate(dividend.getPaymentDate());
        response.setCreatedAt(dividend.getCreatedAt());
        return response;
    }
}
