package com.portfoliomanager.service;

import com.portfoliomanager.dto.pnl.CostBasisResult;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Comparator;
import java.math.RoundingMode;
import java.util.List;

/**
 * Pure calculation logic for weighted-average cost basis and historical realised PNL.
 * No Spring context.
 */
public class CostBasisCalculator {

    private CostBasisCalculator() {}

    public static CostBasisResult calculate(List<Transaction> transactions) {
        return calculate(transactions, null, null);
    }

    public static CostBasisResult calculate(List<Transaction> transactions, String homeCurrency, FxRateService fxRateService) {
        // Keep same-day operations deterministic: apply earlier created rows first.
        // This prevents SELL-before-BUY mis-ordering when txn_date is the same.
        List<Transaction> orderedTransactions = transactions.stream()
            .sorted(Comparator
                .comparing(Transaction::getTxnDate, Comparator.nullsLast(LocalDate::compareTo))
                .thenComparing(Transaction::getCreatedAt, Comparator.nullsLast(Instant::compareTo))
                .thenComparing(t -> t.getId() == null ? "" : t.getId().toString()))
            .toList();

        BigDecimal qty = BigDecimal.ZERO;
        BigDecimal costLocal = BigDecimal.ZERO;
        BigDecimal costHome = BigDecimal.ZERO;
        BigDecimal realisedLocal = BigDecimal.ZERO;
        BigDecimal realisedHome = BigDecimal.ZERO;

        for (Transaction t : orderedTransactions) {
            BigDecimal tQty = t.getQuantity() != null ? t.getQuantity() : BigDecimal.ZERO;
            BigDecimal tPrice = t.getPrice() != null ? t.getPrice() : BigDecimal.ZERO;
            BigDecimal tFx = resolveFxRate(t, homeCurrency, fxRateService);

            if (t.getType() == TransactionType.BUY || t.getType() == TransactionType.DEPOSIT) {
                qty = qty.add(tQty);
                costLocal = costLocal.add(tQty.multiply(tPrice));
                costHome = costHome.add(CurrencyConverter.toHomeHistorical(tQty.multiply(tPrice), tFx));
                
            } else if (t.getType() == TransactionType.SELL || t.getType() == TransactionType.WITHDRAWAL) {
                if (qty.compareTo(BigDecimal.ZERO) == 0) continue; // Skip sells if no holdings

                // Cap sell quantity at available holdings (prevents negative positions from bad data)
                BigDecimal effectiveSellQty = tQty.min(qty);

                // Weighted average cost at the exact moment before the sell
                BigDecimal avgCostLocal = costLocal.divide(qty, 8, RoundingMode.HALF_UP);
                BigDecimal avgCostHome = costHome.divide(qty, 8, RoundingMode.HALF_UP);

                // Realised PNL = sold_qty * (sell_price - avg_cost)
                BigDecimal pnlLocal = effectiveSellQty.multiply(tPrice.subtract(avgCostLocal));
                
                // For home currency: (sold_qty * sell_price * historical_sell_fx) - (sold_qty * avg_cost_home)
                BigDecimal sellProceedsHome = CurrencyConverter.toHomeHistorical(tPrice, tFx);
                BigDecimal pnlHome = effectiveSellQty.multiply(sellProceedsHome.subtract(avgCostHome));

                realisedLocal = realisedLocal.add(pnlLocal);
                realisedHome = realisedHome.add(pnlHome);

                // Deduct the sold quantity from the pool at the average cost
                qty = qty.subtract(effectiveSellQty);
                costLocal = qty.multiply(avgCostLocal);
                costHome = qty.multiply(avgCostHome);
            }
        }

        BigDecimal finalAvgLocal = qty.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : costLocal.divide(qty, 8, RoundingMode.HALF_UP);
        BigDecimal finalAvgHome = qty.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : costHome.divide(qty, 8, RoundingMode.HALF_UP);

        return new CostBasisResult(qty, finalAvgLocal, finalAvgHome, realisedLocal, realisedHome);
    }

    private static BigDecimal resolveFxRate(Transaction transaction, String homeCurrency, FxRateService fxRateService) {
        if (transaction.getFxRateToHome() != null && transaction.getFxRateToHome().compareTo(BigDecimal.ZERO) > 0) {
            return transaction.getFxRateToHome();
        }

        String sourceCurrency = transaction.getCurrency();
        if (sourceCurrency == null || sourceCurrency.isBlank() || homeCurrency == null || homeCurrency.isBlank()) {
            return BigDecimal.ONE;
        }
        if (sourceCurrency.equalsIgnoreCase(homeCurrency)) {
            return BigDecimal.ONE;
        }

        if (fxRateService == null || transaction.getTxnDate() == null) {
            return BigDecimal.ONE;
        }

        return fxRateService.getRate(sourceCurrency, homeCurrency, transaction.getTxnDate()).orElse(BigDecimal.ONE);
    }
}
