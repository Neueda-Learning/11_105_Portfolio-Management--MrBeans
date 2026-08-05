package com.portfoliomanager.service;

import com.portfoliomanager.dto.pnl.CostBasisResult;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Pure calculation logic for weighted-average cost basis and historical realised PNL.
 * No Spring context.
 */
public class CostBasisCalculator {

    private CostBasisCalculator() {}

    public static CostBasisResult calculate(List<Transaction> transactions) {
        BigDecimal qty = BigDecimal.ZERO;
        BigDecimal costLocal = BigDecimal.ZERO;
        BigDecimal costHome = BigDecimal.ZERO;
        BigDecimal realisedLocal = BigDecimal.ZERO;
        BigDecimal realisedHome = BigDecimal.ZERO;

        for (Transaction t : transactions) {
            BigDecimal tQty = t.getQuantity() != null ? t.getQuantity() : BigDecimal.ZERO;
            BigDecimal tPrice = t.getPrice() != null ? t.getPrice() : BigDecimal.ZERO;
            BigDecimal tFx = t.getFxRateToHome() != null ? t.getFxRateToHome() : BigDecimal.ONE;

            if (t.getType() == TransactionType.BUY || t.getType() == TransactionType.DEPOSIT) {
                qty = qty.add(tQty);
                costLocal = costLocal.add(tQty.multiply(tPrice));
                costHome = costHome.add(CurrencyConverter.toHomeHistorical(tQty.multiply(tPrice), tFx));
                
            } else if (t.getType() == TransactionType.SELL || t.getType() == TransactionType.WITHDRAWAL) {
                if (qty.compareTo(BigDecimal.ZERO) == 0) continue; // Skip sells if no holdings

                // Weighted average cost at the exact moment before the sell
                BigDecimal avgCostLocal = costLocal.divide(qty, 8, RoundingMode.HALF_UP);
                BigDecimal avgCostHome = costHome.divide(qty, 8, RoundingMode.HALF_UP);

                // Realised PNL = sold_qty * (sell_price - avg_cost)
                BigDecimal pnlLocal = tQty.multiply(tPrice.subtract(avgCostLocal));
                
                // For home currency: (sold_qty * sell_price * historical_sell_fx) - (sold_qty * avg_cost_home)
                BigDecimal sellProceedsHome = CurrencyConverter.toHomeHistorical(tPrice, tFx);
                BigDecimal pnlHome = tQty.multiply(sellProceedsHome.subtract(avgCostHome));

                realisedLocal = realisedLocal.add(pnlLocal);
                realisedHome = realisedHome.add(pnlHome);

                // Deduct the sold quantity from the pool at the average cost
                qty = qty.subtract(tQty);
                costLocal = qty.multiply(avgCostLocal);
                costHome = qty.multiply(avgCostHome);
            }
        }

        BigDecimal finalAvgLocal = qty.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : costLocal.divide(qty, 8, RoundingMode.HALF_UP);
        BigDecimal finalAvgHome = qty.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : costHome.divide(qty, 8, RoundingMode.HALF_UP);

        return new CostBasisResult(qty, finalAvgLocal, finalAvgHome, realisedLocal, realisedHome);
    }
}
