package com.portfoliomanager.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FrankfurterFxRateClientTest {

    @Mock
    private RestTemplate restTemplate;

    private FrankfurterFxRateClient client;

    @BeforeEach
    void setUp() throws Exception {
        client = new FrankfurterFxRateClient();
        // Inject mock RestTemplate via reflection
        var field = FrankfurterFxRateClient.class.getDeclaredField("restTemplate");
        field.setAccessible(true);
        field.set(client, restTemplate);
    }

    @Test
    void getRates_NullInput_ReturnsEmptyMap() {
        assertTrue(client.getRates(null).isEmpty());
    }

    @Test
    void getRates_EmptyInput_ReturnsEmptyMap() {
        assertTrue(client.getRates(List.of()).isEmpty());
    }

    @Test
    void getRates_SameCurrency_ReturnsOne() {
        Map<String, FxRateClient.CurrentRate> result = client.getRates(List.of("USDUSD=X"));
        assertEquals(BigDecimal.ONE, result.get("USDUSD=X").rate());
        verify(restTemplate, never()).getForObject(anyString(), any(), anyString(), anyString());
    }

    @Test
    void getRates_ShortPairSkipped() {
        Map<String, FxRateClient.CurrentRate> result = client.getRates(List.of("AB"));
        assertTrue(result.isEmpty());
    }

    @Test
    void getRates_ValidPair_ParsesResponse() throws Exception {
        // Create response using reflection since it's a private static class
        Class<?> responseClass = Class.forName("com.portfoliomanager.client.FrankfurterFxRateClient$FrankfurterResponse");
        java.lang.reflect.Constructor<?> constructor = responseClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        Object response = constructor.newInstance();
        var ratesField = responseClass.getDeclaredField("rates");
        ratesField.setAccessible(true);
        ratesField.set(response, Map.of("USD", new BigDecimal("1.09")));

        when(restTemplate.getForObject(anyString(), any(Class.class), eq("EUR"), eq("USD")))
                .thenReturn(response);

        Map<String, FxRateClient.CurrentRate> result = client.getRates(List.of("EURUSD=X"));
        assertEquals(1, result.size());
        assertEquals(new BigDecimal("1.09"), result.get("EURUSD=X").rate());
    }

    @Test
    void getRates_ApiReturnsNull_SkipsPair() {
        when(restTemplate.getForObject(anyString(), any(Class.class), eq("EUR"), eq("USD")))
                .thenReturn(null);

        Map<String, FxRateClient.CurrentRate> result = client.getRates(List.of("EURUSD=X"));
        assertTrue(result.isEmpty());
    }

    @Test
    void getRates_ApiThrows_ReturnsPartialResults() {
        when(restTemplate.getForObject(anyString(), any(Class.class), anyString(), anyString()))
                .thenThrow(new RuntimeException("Network error"));

        Map<String, FxRateClient.CurrentRate> result = client.getRates(List.of("EURUSD=X", "GBPUSD=X"));
        assertTrue(result.isEmpty());
    }
}
