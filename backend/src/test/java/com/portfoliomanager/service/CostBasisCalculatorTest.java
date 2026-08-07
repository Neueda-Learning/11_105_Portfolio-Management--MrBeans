package com.portfoliomanager.service;

import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;
import com.portfoliomanager.dto.pnl.CostBasisResult;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CostBasisCalculatorTest {

    @Test
    void calculate_MultipleBuys_AveragesCost() {
        Transaction t1 = createTx(TransactionType.BUY, "10", "100.0", "1.0");
        Transaction t2 = createTx(TransactionType.BUY, "10", "150.0", "2.0");

        CostBasisResult result = CostBasisCalculator.calculate(List.of(t1, t2));

        assertEquals(new BigDecimal("20"), result.totalQuantity());
        // Local cost: (1000 + 1500) / 20 = 125
        assertEquals(0, new BigDecimal("125.00000000").compareTo(result.avgCostLocal()));
        // Home cost: (1000*1.0 + 1500*2.0) = 4000 / 20 = 200
        assertEquals(0, new BigDecimal("200.00000000").compareTo(result.avgCostHome()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.realisedPnlLocal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.realisedPnlHome()));
    }

    @Test
    void calculate_PartialSell_CalculatesRealisedPnlAndMaintainsAvg() {
        Transaction t1 = createTx(TransactionType.BUY, "10", "100.0", "1.0");
        Transaction t2 = createTx(TransactionType.BUY, "10", "150.0", "2.0");
        // Avg Cost = Local 125, Home 200
        Transaction t3 = createTx(TransactionType.SELL, "10", "200.0", "1.5");
        // Local PNL = 10 * (200 - 125) = 750
        // Home PNL = (10 * 200 * 1.5) - (10 * 200) = 3000 - 2000 = 1000

        CostBasisResult result = CostBasisCalculator.calculate(List.of(t1, t2, t3));

        assertEquals(0, new BigDecimal("10").compareTo(result.totalQuantity()));
        assertEquals(0, new BigDecimal("125.00000000").compareTo(result.avgCostLocal()));
        assertEquals(0, new BigDecimal("200.00000000").compareTo(result.avgCostHome()));
        
        assertEquals(0, new BigDecimal("750.00000000").compareTo(result.realisedPnlLocal()));
        assertEquals(0, new BigDecimal("1000.00000000").compareTo(result.realisedPnlHome()));
    }

    @Test
    void calculate_SellAll_ResetsAverageToZero() {
        Transaction t1 = createTx(TransactionType.BUY, "10", "100.0", "1.0");
        Transaction t2 = createTx(TransactionType.SELL, "10", "200.0", "1.0");

        CostBasisResult result = CostBasisCalculator.calculate(List.of(t1, t2));

        assertEquals(0, BigDecimal.ZERO.compareTo(result.totalQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.avgCostLocal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.avgCostHome()));
        
        assertEquals(0, new BigDecimal("1000.00000000").compareTo(result.realisedPnlLocal()));
    }

    @Test
    void calculate_SellBeforeBuy_GracefullySkips() {
        // Technically a short sell, but MVP handles it by ignoring negative balance sells
        Transaction t1 = createTx(TransactionType.SELL, "10", "200.0", "1.0");
        Transaction t2 = createTx(TransactionType.BUY, "10", "100.0", "1.0");

        CostBasisResult result = CostBasisCalculator.calculate(List.of(t1, t2));

        assertEquals(0, new BigDecimal("10").compareTo(result.totalQuantity()));
        assertEquals(0, new BigDecimal("100.00000000").compareTo(result.avgCostLocal()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.realisedPnlLocal())); // Sell was ignored
    }

    @Test
    void calculate_HandlesNullFieldsGracefully() {
        Transaction t1 = new Transaction();
        t1.setType(TransactionType.BUY);
        // Qty, price, FX are null

        CostBasisResult result = CostBasisCalculator.calculate(List.of(t1));

        assertEquals(0, BigDecimal.ZERO.compareTo(result.totalQuantity()));
        assertEquals(0, BigDecimal.ZERO.compareTo(result.avgCostLocal()));
    }

    @Test
    void calculate_SameDayBuyAndSell_UsesCreatedAtTieBreaker() {
        LocalDate sameDay = LocalDate.of(2026, 8, 7);

        Transaction buy = createTx(TransactionType.BUY, "10", "100.0", "1.0");
        buy.setTxnDate(sameDay);
        buy.setCreatedAt(Instant.parse("2026-08-07T10:00:00Z"));

        Transaction sell = createTx(TransactionType.SELL, "5", "110.0", "1.0");
        sell.setTxnDate(sameDay);
        sell.setCreatedAt(Instant.parse("2026-08-07T10:05:00Z"));

        // Input order intentionally reversed; calculator should still process BUY first.
        CostBasisResult result = CostBasisCalculator.calculate(List.of(sell, buy));

        assertEquals(0, new BigDecimal("5").compareTo(result.totalQuantity()));
        assertEquals(0, new BigDecimal("100.00000000").compareTo(result.avgCostLocal()));
        assertEquals(0, new BigDecimal("50.00000000").compareTo(result.realisedPnlLocal()));
        assertEquals(0, new BigDecimal("50.00000000").compareTo(result.realisedPnlHome()));
    }

    private Transaction createTx(TransactionType type, String qty, String price, String fx) {
        Transaction tx = new Transaction();
        tx.setType(type);
        tx.setQuantity(new BigDecimal(qty));
        tx.setPrice(new BigDecimal(price));
        tx.setFxRateToHome(new BigDecimal(fx));
        return tx;
    }
}
