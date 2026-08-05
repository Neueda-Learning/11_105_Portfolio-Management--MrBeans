package com.portfoliomanager.service;

import com.portfoliomanager.util.MoneyMath;
import com.portfoliomanager.dto.pnl.CostBasisResult;
import com.portfoliomanager.dto.pnl.PnlResult;

import java.math.BigDecimal;

/**
 * Pure calculation logic for Unrealised PNL. 
 * Realised and Unrealised PNL are strictly separated per PRD rules.
 */
public class PnlCalculator {

    private PnlCalculator() {}

    public static PnlResult calculate(CostBasisResult costBasis, BigDecimal currentPriceLocal, BigDecimal currentFxRate) {
        BigDecimal qty = costBasis.totalQuantity();
        BigDecimal price = currentPriceLocal != null ? currentPriceLocal : BigDecimal.ZERO;

        // 1. Local Unrealised = Current Value - Cost Basis
        BigDecimal currentValueLocal = qty.multiply(price);
        BigDecimal totalCostLocal = qty.multiply(costBasis.avgCostLocal());
        BigDecimal unrealisedLocal = currentValueLocal.subtract(totalCostLocal);

        // 2. Home Unrealised (Dual FX Rule) = (Current Value * Live FX) - (Total Cost Home)
        BigDecimal currentValueHome = CurrencyConverter.toHomeCurrent(currentValueLocal, currentFxRate);
        BigDecimal totalCostHome = qty.multiply(costBasis.avgCostHome());
        BigDecimal unrealisedHome = currentValueHome.subtract(totalCostHome);

        return new PnlResult(
                MoneyMath.roundCurrency(costBasis.realisedPnlHome()),
                MoneyMath.roundCurrency(unrealisedHome),
                MoneyMath.roundCurrency(costBasis.realisedPnlLocal()),
                MoneyMath.roundCurrency(unrealisedLocal),
                MoneyMath.roundCurrency(totalCostHome),
                MoneyMath.roundQuantity(qty)
        );
    }
}
