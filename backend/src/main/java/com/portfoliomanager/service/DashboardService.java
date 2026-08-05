package com.portfoliomanager.service;

import com.portfoliomanager.util.MoneyMath;
import com.portfoliomanager.dto.dashboard.AllocationResponse;
import com.portfoliomanager.dto.dashboard.DashboardSummaryResponse;
import com.portfoliomanager.dto.dashboard.TrendResponse;
import com.portfoliomanager.service.FxRateService;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.service.CostBasisCalculator;
import com.portfoliomanager.service.PnlCalculator;
import com.portfoliomanager.dto.pnl.CostBasisResult;
import com.portfoliomanager.dto.pnl.PnlResult;
import com.portfoliomanager.model.PriceSnapshot;
import com.portfoliomanager.repository.PriceSnapshotRepository;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.repository.TransactionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final InvestmentRepository investmentRepository;
    private final TransactionRepository transactionRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final FxRateService fxRateService;

    public DashboardService(InvestmentRepository investmentRepository,
                            TransactionRepository transactionRepository,
                            PriceSnapshotRepository priceSnapshotRepository,
                            FxRateService fxRateService) {
        this.investmentRepository = investmentRepository;
        this.transactionRepository = transactionRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.fxRateService = fxRateService;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(String homeCurrency) {
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalRealised = BigDecimal.ZERO;
        BigDecimal totalUnrealised = BigDecimal.ZERO;

        List<Investment> investments = investmentRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Investment inv : investments) {
            PnlResult pnl = calculateCurrentPnl(inv, homeCurrency, today);
            totalCost = totalCost.add(pnl.totalCostBasis());
            totalRealised = totalRealised.add(pnl.realisedPnl());
            totalUnrealised = totalUnrealised.add(pnl.unrealisedPnl());
        }

        DashboardSummaryResponse res = new DashboardSummaryResponse();
        res.setTotalCostBasis(MoneyMath.roundCurrency(totalCost));
        res.setTotalRealisedPnl(MoneyMath.roundCurrency(totalRealised));
        res.setTotalUnrealisedPnl(MoneyMath.roundCurrency(totalUnrealised));
        // Total Value = Cost Basis + Unrealised Pnl
        res.setTotalValue(MoneyMath.roundCurrency(totalCost.add(totalUnrealised)));
        return res;
    }

    @Transactional(readOnly = true)
    public List<AllocationResponse> getAllocation(String homeCurrency) {
        Map<InvestmentType, BigDecimal> valueByType = new EnumMap<>(InvestmentType.class);
        BigDecimal totalPortfolioValue = BigDecimal.ZERO;

        List<Investment> investments = investmentRepository.findAll();
        LocalDate today = LocalDate.now();

        for (Investment inv : investments) {
            PnlResult pnl = calculateCurrentPnl(inv, homeCurrency, today);
            BigDecimal currentValue = pnl.totalCostBasis().add(pnl.unrealisedPnl());

            valueByType.merge(inv.getType(), currentValue, BigDecimal::add);
            totalPortfolioValue = totalPortfolioValue.add(currentValue);
        }

        List<AllocationResponse> allocations = new ArrayList<>();
        for (Map.Entry<InvestmentType, BigDecimal> entry : valueByType.entrySet()) {
            BigDecimal percentage = BigDecimal.ZERO;
            if (totalPortfolioValue.compareTo(BigDecimal.ZERO) > 0) {
                percentage = entry.getValue()
                        .multiply(new BigDecimal("100"))
                        .divide(totalPortfolioValue, 2, RoundingMode.HALF_UP);
            }
            allocations.add(new AllocationResponse(entry.getKey(), MoneyMath.roundCurrency(entry.getValue()), percentage));
        }

        return allocations;
    }

    @Transactional(readOnly = true)
    public List<TrendResponse> getTrend(String homeCurrency, int days) {
        List<TrendResponse> trend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        List<Investment> investments = investmentRepository.findAll();

        Map<UUID, List<Transaction>> txMap = new HashMap<>();
        for (Investment inv : investments) {
            txMap.put(inv.getId(), transactionRepository.findByInvestmentIdOrderByTxnDateAsc(inv.getId()));
        }

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            BigDecimal dailyTotalValue = BigDecimal.ZERO;

            for (Investment inv : investments) {
                List<Transaction> historicalTxns = txMap.get(inv.getId()).stream()
                        .filter(t -> !t.getTxnDate().isAfter(date))
                        .collect(Collectors.toList());

                CostBasisResult cb = CostBasisCalculator.calculate(historicalTxns);
                BigDecimal price = getLatestPriceBeforeDate(inv.getId(), date);
                BigDecimal fx = fxRateService.getRate(inv.getCurrency(), homeCurrency, date).orElse(BigDecimal.ONE);

                PnlResult pnl = PnlCalculator.calculate(cb, price, fx);
                dailyTotalValue = dailyTotalValue.add(pnl.totalCostBasis().add(pnl.unrealisedPnl()));
            }
            trend.add(new TrendResponse(date, MoneyMath.roundCurrency(dailyTotalValue)));
        }
        return trend;
    }

    private PnlResult calculateCurrentPnl(Investment inv, String homeCurrency, LocalDate date) {
        List<Transaction> txns = transactionRepository.findByInvestmentIdOrderByTxnDateAsc(inv.getId());
        CostBasisResult cb = CostBasisCalculator.calculate(txns);

        BigDecimal currentPrice = getLatestPriceBeforeDate(inv.getId(), date);
        BigDecimal fxRate = fxRateService.getRate(inv.getCurrency(), homeCurrency, date).orElse(BigDecimal.ONE);

        return PnlCalculator.calculate(cb, currentPrice, fxRate);
    }

    private BigDecimal getLatestPriceBeforeDate(UUID investmentId, LocalDate date) {
        List<PriceSnapshot> snapshots = priceSnapshotRepository.findByInvestmentIdOrderByFetchedAtDesc(investmentId);
        return snapshots.stream()
                .filter(s -> !s.getFetchedAt().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(date))
                .map(PriceSnapshot::getPrice)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }
}
