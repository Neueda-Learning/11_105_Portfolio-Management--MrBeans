package com.portfoliomanager.service;

import com.portfoliomanager.util.MoneyMath;
import com.portfoliomanager.dto.dashboard.AllocationResponse;
import com.portfoliomanager.dto.dashboard.DashboardSummaryResponse;
import com.portfoliomanager.dto.dashboard.TrendResponse;
import com.portfoliomanager.service.FxRateService;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.repository.DividendRepository;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
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
    private final DividendRepository dividendRepository;

    public DashboardService(InvestmentRepository investmentRepository,
                            TransactionRepository transactionRepository,
                            PriceSnapshotRepository priceSnapshotRepository,
                            FxRateService fxRateService,
                            DividendRepository dividendRepository) {
        this.investmentRepository = investmentRepository;
        this.transactionRepository = transactionRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.fxRateService = fxRateService;
        this.dividendRepository = dividendRepository;
    }

    @Cacheable(value = "dashboard-summary", key = "#homeCurrency")
    @Transactional
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

        // Dividend income this calendar year (net of withholding tax)
        LocalDate yearStart = LocalDate.of(today.getYear(), 1, 1);
        LocalDate yearEnd   = LocalDate.of(today.getYear(), 12, 31);
        BigDecimal dividendIncome = dividendRepository
                .sumNetAmountByPaymentDateBetween(yearStart, yearEnd)
                .orElse(BigDecimal.ZERO);
        res.setDividendIncomeThisYear(MoneyMath.roundCurrency(dividendIncome));

        return res;
    }

    @Cacheable(value = "dashboard-allocation", key = "#homeCurrency")
    @Transactional
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

    @Transactional
    public List<TrendResponse> getTrend(String homeCurrency, int days) {
        return getTrendFiltered(homeCurrency, null, null, null, days);
    }

    @Transactional
    public List<TrendResponse> getTrendFiltered(String homeCurrency,
                                                LocalDate fromDate,
                                                LocalDate toDate,
                                                List<InvestmentType> types,
                                                int days) {
        if (days <= 0) {
            throw new IllegalArgumentException("days must be greater than 0");
        }

        LocalDate today = LocalDate.now();
        LocalDate startDate;
        LocalDate endDate;

        if (fromDate != null || toDate != null) {
            if (fromDate == null || toDate == null) {
                throw new IllegalArgumentException("Both fromDate and toDate must be provided together");
            }
            if (fromDate.isAfter(toDate)) {
                throw new IllegalArgumentException("fromDate must be before or equal to toDate");
            }

            long totalDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
            if (totalDays > 730) {
                throw new IllegalArgumentException("Date range cannot exceed 730 days");
            }

            startDate = fromDate;
            endDate = toDate;
        } else {
            if (days > 730) {
                throw new IllegalArgumentException("days cannot exceed 730");
            }
            startDate = today.minusDays(days - 1L);
            endDate = today;
        }

        List<TrendResponse> trend = new ArrayList<>();

        List<Investment> investments = investmentRepository.findAll();
        if (types != null && !types.isEmpty()) {
            Set<InvestmentType> selectedTypes = EnumSet.copyOf(types);
            investments = investments.stream()
                    .filter(inv -> selectedTypes.contains(inv.getType()))
                    .collect(Collectors.toList());
        }

        Map<UUID, List<Transaction>> txMap = new HashMap<>();
        for (Investment inv : investments) {
            txMap.put(inv.getId(), transactionRepository.findByInvestmentIdOrderByTxnDateAsc(inv.getId()));
        }

        // Pre-fetch a single FX rate per investment (most-recent stored rate).
        // FX rates don't change meaningfully day-to-day within a 30-day trend window,
        // and fetching per-date would trigger N×M Yahoo Finance API calls causing
        // rate-limiting (HTTP 429) and 72+ second response times.
        Map<UUID, BigDecimal> fxMap = new HashMap<>();
        for (Investment inv : investments) {
            BigDecimal fx = fxRateService.getLatestRate(inv.getCurrency(), homeCurrency).orElse(BigDecimal.ONE);
            fxMap.put(inv.getId(), fx);
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate evaluationDate = date;
            BigDecimal dailyMarketValue = BigDecimal.ZERO;
            BigDecimal dailyRealisedPnl = BigDecimal.ZERO;
            BigDecimal dailyInvested = BigDecimal.ZERO;

            for (Investment inv : investments) {
                List<Transaction> historicalTxns = txMap.get(inv.getId()).stream()
                        .filter(t -> !t.getTxnDate().isAfter(evaluationDate))
                        .collect(Collectors.toList());

                CostBasisResult cb = CostBasisCalculator.calculate(historicalTxns);
                BigDecimal price = getLatestPriceBeforeDate(inv.getId(), evaluationDate);
                BigDecimal fx = fxMap.get(inv.getId());

                PnlResult pnl = PnlCalculator.calculate(cb, price, fx);
                dailyMarketValue = dailyMarketValue.add(pnl.totalCostBasis().add(pnl.unrealisedPnl()));
                dailyRealisedPnl = dailyRealisedPnl.add(pnl.realisedPnl());
                dailyInvested = dailyInvested.add(pnl.totalCostBasis());
            }
            BigDecimal totalWealth = dailyMarketValue.add(dailyRealisedPnl);
            trend.add(new TrendResponse(evaluationDate, MoneyMath.roundCurrency(totalWealth), MoneyMath.roundCurrency(dailyInvested)));
        }
        return trend;
    }

    @Transactional
    public com.portfoliomanager.dto.pnl.InvestmentPnlResponse getInvestmentPnl(java.util.UUID investmentId, String homeCurrency) {
        Investment inv = investmentRepository.findById(investmentId)
                .orElseThrow(() -> new com.portfoliomanager.exception.ResourceNotFoundException("Investment not found: " + investmentId));
        PnlResult pnl = calculateCurrentPnl(inv, homeCurrency, LocalDate.now());
        com.portfoliomanager.dto.pnl.InvestmentPnlResponse res = new com.portfoliomanager.dto.pnl.InvestmentPnlResponse();
        res.setRealisedPnl(MoneyMath.roundCurrency(pnl.realisedPnl()));
        res.setUnrealisedPnl(MoneyMath.roundCurrency(pnl.unrealisedPnl()));
        res.setRealisedPnlLocal(MoneyMath.roundCurrency(pnl.realisedPnlLocal()));
        res.setUnrealisedPnlLocal(MoneyMath.roundCurrency(pnl.unrealisedPnlLocal()));
        res.setTotalCostBasis(MoneyMath.roundCurrency(pnl.totalCostBasis()));
        res.setCurrentQuantity(pnl.currentQuantity());
        return res;
    }

    @Transactional
    @Cacheable(value = "dashboard-performance", key = "#homeCurrency")
    public List<com.portfoliomanager.dto.dashboard.PerformanceResponse> getPerformance(String homeCurrency) {
        List<Investment> investments = investmentRepository.findAll();
        LocalDate today = LocalDate.now();
        List<com.portfoliomanager.dto.dashboard.PerformanceResponse> result = new ArrayList<>();

        for (Investment inv : investments) {
            PnlResult pnl = calculateCurrentPnl(inv, homeCurrency, today);
            BigDecimal costBasis = pnl.totalCostBasis();
            BigDecimal currentValue = costBasis.add(pnl.unrealisedPnl());

            // Total return = unrealised + realised PnL, relative to remaining cost basis
            BigDecimal totalPnl = pnl.unrealisedPnl().add(pnl.realisedPnl());
            BigDecimal returnPct = BigDecimal.ZERO;
            if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                returnPct = totalPnl
                        .multiply(new BigDecimal("100"))
                        .divide(costBasis, 2, RoundingMode.HALF_UP);
            }

            BigDecimal riskScore = calculateVolatility(inv.getId());
            if (riskScore == null) riskScore = typeRiskProxy(inv.getType());

            com.portfoliomanager.dto.dashboard.PerformanceResponse p = new com.portfoliomanager.dto.dashboard.PerformanceResponse();
            p.setSymbol(inv.getSymbol());
            p.setName(inv.getName());
            p.setReturnPct(returnPct);
            p.setRiskScore(riskScore);
            p.setCurrentValue(MoneyMath.roundCurrency(currentValue));
            p.setInvestmentType(inv.getType().name());
            result.add(p);
        }
        return result;
    }

    private BigDecimal calculateVolatility(UUID investmentId) {
        List<PriceSnapshot> snapshots = priceSnapshotRepository.findByInvestmentIdOrderByFetchedAtDesc(investmentId);
        if (snapshots.size() < 3) return null;

        List<BigDecimal> prices = snapshots.stream()
                .limit(30)
                .map(PriceSnapshot::getPrice)
                .collect(Collectors.toList());
        Collections.reverse(prices);

        List<Double> rets = new ArrayList<>();
        for (int i = 1; i < prices.size(); i++) {
            BigDecimal prev = prices.get(i - 1);
            if (prev.compareTo(BigDecimal.ZERO) > 0) {
                double r = prices.get(i).subtract(prev).divide(prev, 8, RoundingMode.HALF_UP).doubleValue();
                rets.add(r);
            }
        }
        if (rets.isEmpty()) return null;

        double mean = rets.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = rets.stream().mapToDouble(r -> (r - mean) * (r - mean)).average().orElse(0);
        double stdDev = Math.sqrt(variance) * 100;
        return BigDecimal.valueOf(stdDev).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal typeRiskProxy(InvestmentType type) {
        return switch (type) {
            case STOCK -> new BigDecimal("20.0");
            case BOND  -> new BigDecimal("5.0");
            case CASH  -> new BigDecimal("1.0");
            default    -> new BigDecimal("25.0");
        };
    }

    private PnlResult calculateCurrentPnl(Investment inv, String homeCurrency, LocalDate date) {
        List<Transaction> txns = transactionRepository.findByInvestmentIdOrderByTxnDateAsc(inv.getId())
                .stream()
                .filter(t -> !t.getTxnDate().isAfter(date))
                .collect(Collectors.toList());
        CostBasisResult cb = CostBasisCalculator.calculate(txns, homeCurrency, fxRateService);

        BigDecimal currentPrice = getLatestPriceBeforeDate(inv.getId(), date);
        // getRate() now resolves via inverse / cross-rate / on-demand fetch before falling back to 1.0
        BigDecimal fxRate = fxRateService.getRate(inv.getCurrency(), homeCurrency, date).orElse(BigDecimal.ONE);

        return PnlCalculator.calculate(cb, currentPrice, fxRate);
    }

    private BigDecimal getLatestPriceBeforeDate(UUID investmentId, LocalDate date) {
        List<PriceSnapshot> snapshots = priceSnapshotRepository.findByInvestmentIdOrderByFetchedAtDesc(investmentId);
        return snapshots.stream()
                .filter(s -> !s.getFetchedAt().atZone(ZoneId.systemDefault()).toLocalDate().isAfter(date))
                .map(PriceSnapshot::getPrice)
                .findFirst()
                .orElse(null); // null = no market price available
    }
}
