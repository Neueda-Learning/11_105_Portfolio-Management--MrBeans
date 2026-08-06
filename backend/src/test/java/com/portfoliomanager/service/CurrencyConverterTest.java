package com.portfoliomanager.service;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyConverterTest {

    @Test
    void toHomeHistorical_WithValidValues() {
        BigDecimal local = new BigDecimal("100.00");
        BigDecimal fx = new BigDecimal("1.50");
        assertEquals(new BigDecimal("150.0000"), CurrencyConverter.toHomeHistorical(local, fx));
    }

    @Test
    void toHomeHistorical_WithNullRate_DefaultsToOne() {
        BigDecimal local = new BigDecimal("100.00");
        assertEquals(new BigDecimal("100.00"), CurrencyConverter.toHomeHistorical(local, null));
    }

    @Test
    void toHomeHistorical_WithNullAmount_ReturnsZero() {
        assertEquals(BigDecimal.ZERO, CurrencyConverter.toHomeHistorical(null, new BigDecimal("1.5")));
    }

    @Test
    void toHomeCurrent_WithValidValues() {
        BigDecimal local = new BigDecimal("250.50");
        BigDecimal fx = new BigDecimal("2.0");
        assertEquals(new BigDecimal("501.000"), CurrencyConverter.toHomeCurrent(local, fx));
    }

    @Test
    void toHomeCurrent_WithNullRate_DefaultsToOne() {
        BigDecimal local = new BigDecimal("250.50");
        assertEquals(new BigDecimal("250.50"), CurrencyConverter.toHomeCurrent(local, null));
    }

    @Test
    void toHomeCurrent_WithNullAmount_ReturnsZero() {
        assertEquals(BigDecimal.ZERO, CurrencyConverter.toHomeCurrent(null, new BigDecimal("1.5")));
    }
}
