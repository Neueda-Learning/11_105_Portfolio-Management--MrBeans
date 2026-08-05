package com.portfoliomanager.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Centralized helpers for money and FX math.
 */
public final class MoneyMath {

    private MoneyMath() {
        // Utility class
    }

    /**
     * Rounds amounts of currency (e.g., P&L, transaction totals) to 2 decimal places using HALF_UP.
     */
    public static BigDecimal roundCurrency(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Rounds FX rates or precision-sensitive prices to 4 decimal places using HALF_UP.
     */
    public static BigDecimal roundRate(BigDecimal rate) {
        if (rate == null) {
            return null;
        }
        return rate.setScale(4, RoundingMode.HALF_UP);
    }

    /**
     * Rounds share quantities and precise FX rates to 8 decimal places using HALF_UP.
     */
    public static BigDecimal roundQuantity(BigDecimal quantity) {
        if (quantity == null) {
            return null;
        }
        return quantity.setScale(8, RoundingMode.HALF_UP);
    }
}
