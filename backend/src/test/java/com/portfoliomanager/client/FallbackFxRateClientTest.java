package com.portfoliomanager.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FallbackFxRateClientTest {

    private FxRateClient primary;
    private FxRateClient fallback;
    private FallbackFxRateClient client;

    @BeforeEach
    void setUp() throws Exception {
        primary = mock(FxRateClient.class);
        fallback = mock(FxRateClient.class);

        // Use reflection to inject mock clients into FallbackFxRateClient
        client = new FallbackFxRateClient();
        var primaryField = FallbackFxRateClient.class.getDeclaredField("primary");
        primaryField.setAccessible(true);
        primaryField.set(client, primary);
        var fallbackField = FallbackFxRateClient.class.getDeclaredField("fallback");
        fallbackField.setAccessible(true);
        fallbackField.set(client, fallback);
    }

    @Test
    void getRates_PrimarySucceeds_ReturnsPrimaryRates() {
        List<String> pairs = List.of("EURUSD=X");
        Map<String, FxRateClient.CurrentRate> primaryRates = Map.of(
                "EURUSD=X", new FxRateClient.CurrentRate(new BigDecimal("1.09"))
        );
        when(primary.getRates(pairs)).thenReturn(primaryRates);

        Map<String, FxRateClient.CurrentRate> result = client.getRates(pairs);

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("1.09"), result.get("EURUSD=X").rate());
        verify(fallback, never()).getRates(anyList());
    }

    @Test
    void getRates_PrimaryThrows_FallsBackToSecondary() {
        List<String> pairs = List.of("EURUSD=X");
        when(primary.getRates(pairs)).thenThrow(new RuntimeException("Rate limited"));

        Map<String, FxRateClient.CurrentRate> fallbackRates = Map.of(
                "EURUSD=X", new FxRateClient.CurrentRate(new BigDecimal("1.08"))
        );
        when(fallback.getRates(pairs)).thenReturn(fallbackRates);

        Map<String, FxRateClient.CurrentRate> result = client.getRates(pairs);

        assertEquals(new BigDecimal("1.08"), result.get("EURUSD=X").rate());
        verify(fallback).getRates(pairs);
    }

    @Test
    void getRates_PrimaryReturnsEmpty_FallsBackToSecondary() {
        List<String> pairs = List.of("EURUSD=X");
        when(primary.getRates(pairs)).thenReturn(new HashMap<>());

        Map<String, FxRateClient.CurrentRate> fallbackRates = Map.of(
                "EURUSD=X", new FxRateClient.CurrentRate(new BigDecimal("1.08"))
        );
        when(fallback.getRates(pairs)).thenReturn(fallbackRates);

        Map<String, FxRateClient.CurrentRate> result = client.getRates(pairs);

        assertEquals(new BigDecimal("1.08"), result.get("EURUSD=X").rate());
        verify(fallback).getRates(pairs);
    }

    @Test
    void getRates_PrimaryReturnsNull_FallsBackToSecondary() {
        List<String> pairs = List.of("EURUSD=X");
        when(primary.getRates(pairs)).thenReturn(null);

        Map<String, FxRateClient.CurrentRate> fallbackRates = Map.of(
                "EURUSD=X", new FxRateClient.CurrentRate(new BigDecimal("1.07"))
        );
        when(fallback.getRates(pairs)).thenReturn(fallbackRates);

        Map<String, FxRateClient.CurrentRate> result = client.getRates(pairs);

        assertEquals(new BigDecimal("1.07"), result.get("EURUSD=X").rate());
    }

    @Test
    void getRates_BothFail_ReturnsEmptyMap() {
        List<String> pairs = List.of("EURUSD=X");
        when(primary.getRates(pairs)).thenThrow(new RuntimeException("Primary failed"));
        when(fallback.getRates(pairs)).thenThrow(new RuntimeException("Fallback also failed"));

        Map<String, FxRateClient.CurrentRate> result = client.getRates(pairs);

        assertTrue(result.isEmpty());
    }
}
