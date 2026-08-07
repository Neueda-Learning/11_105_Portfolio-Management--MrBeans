package com.portfoliomanager.service;

import com.portfoliomanager.dto.pnl.CostBasisResult;
import com.portfoliomanager.dto.pnl.PnlResult;
import com.portfoliomanager.util.MoneyMath;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PnlCalculatorTest {

    @Test
    void calculate_AppliesDualFxRuleCorrectly() {
        // Assume: Qty 10, Avg Cost Local 125, Avg Cost Home 200, Realised Local 750, Realised Home 1000
        CostBasisResult costBasis = new CostBasisResult(
                new BigDecimal("10.0"),
                new BigDecimal("125.00000000"),
                new BigDecimal("200.00000000"),
                new BigDecimal("750.00000000"),
                new BigDecimal("1000.00000000")
        );

        BigDecimal currentPrice = new BigDecimal("250.00");
        BigDecimal currentFxRate = new BigDecimal("2.0");

        PnlResult result = PnlCalculator.calculate(costBasis, currentPrice, currentFxRate);

        // Unrealised Local: 10 * (250 - 125) = 1250
        assertEquals(new BigDecimal("1250.00"), result.unrealisedPnlLocal());
        
        // Total Cost Home: 10 * 200 = 2000
        assertEquals(new BigDecimal("2000.00"), result.totalCostBasis());
        
        // Unrealised Home: (10 * 250 * 2.0) - 2000 = 5000 - 2000 = 3000
        assertEquals(new BigDecimal("3000.00"), result.unrealisedPnl());

        // Realised should pass through unchanged (but rounded to 2 decimals by MoneyMath)
        assertEquals(new BigDecimal("750.00"), result.realisedPnlLocal());
        assertEquals(new BigDecimal("1000.00"), result.realisedPnl());
        
        assertEquals(new BigDecimal("10.00000000"), result.currentQuantity());
    }

    @Test
    void calculate_HandlesNullPriceGracefully() {
        CostBasisResult costBasis = new CostBasisResult(
                new BigDecimal("10.0"),
                new BigDecimal("100.0"),
                new BigDecimal("100.0"),
                BigDecimal.ZERO,
                BigDecimal.ZERO
        );

        // Null price should be treated as 0 value
        PnlResult result = PnlCalculator.calculate(costBasis, null, new BigDecimal("1.0"));

        // Value is at cost, so unrealised is 0
        assertEquals(new BigDecimal("0.00"), result.unrealisedPnlLocal());
        assertEquals(new BigDecimal("0.00"), result.unrealisedPnl());
    }
}
