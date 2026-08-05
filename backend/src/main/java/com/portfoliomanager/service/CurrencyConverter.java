package com.portfoliomanager.service;

import java.math.BigDecimal;

/**
 * Handles dual FX rule conversions.
 * Section 4.3 dictates that historical transactions are converted using their exact historical rate,
 * whereas current market values are converted using the live/current rate.
 */
public class CurrencyConverter {

    private CurrencyConverter() {}

    /**
     * Converts a historical transaction value to home currency using the historical rate.
     */
    public static BigDecimal toHomeHistorical(BigDecimal localAmount, BigDecimal historicalFxRate) {
        if (localAmount == null) return BigDecimal.ZERO;
        BigDecimal rate = historicalFxRate != null ? historicalFxRate : BigDecimal.ONE;
        return localAmount.multiply(rate);
    }

    /**
     * Converts a current asset value to home currency using the live FX rate.
     */
    public static BigDecimal toHomeCurrent(BigDecimal localAmount, BigDecimal currentFxRate) {
        if (localAmount == null) return BigDecimal.ZERO;
        BigDecimal rate = currentFxRate != null ? currentFxRate : BigDecimal.ONE;
        return localAmount.multiply(rate);
    }
}
