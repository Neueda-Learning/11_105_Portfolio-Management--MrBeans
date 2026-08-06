package com.portfoliomanager.common.util;

import com.portfoliomanager.util.MoneyMath;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MoneyMathTest {

    @Test
    void testRoundCurrency_HandlesRoundingUp() {
        BigDecimal input = new BigDecimal("10.555");
        BigDecimal expected = new BigDecimal("10.56");
        assertEquals(expected, MoneyMath.roundCurrency(input));
    }

    @Test
    void testRoundCurrency_HandlesRoundingDown() {
        BigDecimal input = new BigDecimal("10.554");
        BigDecimal expected = new BigDecimal("10.55");
        assertEquals(expected, MoneyMath.roundCurrency(input));
    }

    @Test
    void testRoundCurrency_HandlesNull() {
        assertNull(MoneyMath.roundCurrency(null));
    }

    @Test
    void testRoundRate_HandlesRoundingUp() {
        BigDecimal input = new BigDecimal("1.12345");
        BigDecimal expected = new BigDecimal("1.1235");
        assertEquals(expected, MoneyMath.roundRate(input));
    }

    @Test
    void testRoundRate_HandlesRoundingDown() {
        BigDecimal input = new BigDecimal("1.12344");
        BigDecimal expected = new BigDecimal("1.1234");
        assertEquals(expected, MoneyMath.roundRate(input));
    }

    @Test
    void testRoundRate_HandlesNull() {
        assertNull(MoneyMath.roundRate(null));
    }
}
