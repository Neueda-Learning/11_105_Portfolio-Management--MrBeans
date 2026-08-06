package com.portfoliomanager.service;

import com.portfoliomanager.repository.FxRateRepository;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.client.FakeFxRateClient;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.FxRate;
import com.portfoliomanager.client.FxRateClient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FxRateServiceTest {

    @Mock
    private FxRateRepository fxRateRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    private FakeFxRateClient fakeFxRateClient;
    private FxRateService fxRateService;

    @BeforeEach
    void setUp() {
        // Use test double exclusively per PRD rules
        fakeFxRateClient = new FakeFxRateClient();
        fxRateService = new FxRateService(fxRateRepository, investmentRepository, fakeFxRateClient);
    }

    @Test
    void getRate_ReturnsOneForSameCurrency() {
        Optional<BigDecimal> rate = fxRateService.getRate("USD", "USD", LocalDate.now());
        assertTrue(rate.isPresent());
        assertEquals(BigDecimal.ONE, rate.get());
        
        // Ensure repository wasn't hit
        verify(fxRateRepository, never()).findByFromCurrencyAndToCurrencyAndRateDate(anyString(), anyString(), any());
    }

    @Test
    void refreshRates_SucceedsForValidPairs() {
        Investment inv1 = new Investment();
        inv1.setCurrency("USD");
        
        Investment inv2 = new Investment();
        inv2.setCurrency("EUR");

        when(investmentRepository.findAll()).thenReturn(List.of(inv1, inv2));
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(anyString(), anyString(), any()))
                .thenReturn(Optional.empty());

        fxRateService.refreshRates();

        // Capture saved rates
        ArgumentCaptor<FxRate> captor = ArgumentCaptor.forClass(FxRate.class);
        verify(fxRateRepository, times(2)).save(captor.capture());

        List<FxRate> savedRates = captor.getAllValues();
        assertEquals(2, savedRates.size());
        
        boolean foundUsd = savedRates.stream().anyMatch(r -> r.getFromCurrency().equals("USD") && r.getRate().compareTo(new BigDecimal("83.50000000")) == 0);
        boolean foundEur = savedRates.stream().anyMatch(r -> r.getFromCurrency().equals("EUR") && r.getRate().compareTo(new BigDecimal("90.10000000")) == 0);
        
        assertTrue(foundUsd);
        assertTrue(foundEur);
    }

    @Test
    void refreshRates_SkipsInvalidPairGracefully() {
        Investment validInv = new Investment();
        validInv.setCurrency("USD");
        
        Investment invalidInv = new Investment();
        invalidInv.setCurrency("GBP"); // GBP is not hardcoded in the Fake Client

        when(investmentRepository.findAll()).thenReturn(List.of(validInv, invalidInv));
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(eq("USD"), eq("INR"), any()))
                .thenReturn(Optional.empty());

        fxRateService.refreshRates();

        // Should only save the USD one. The missing GBP one gracefully logs and continues.
        verify(fxRateRepository, times(1)).save(any(FxRate.class));
    }

    @Test
    void refreshRates_SkipsCycleIfClientThrowsException() {
        FxRateClient failingClient = mock(FxRateClient.class);
        when(failingClient.getRates(anyList())).thenThrow(new RuntimeException("Simulated network failure"));

        FxRateService failingService = new FxRateService(fxRateRepository, investmentRepository, failingClient);
        
        Investment inv1 = new Investment();
        inv1.setCurrency("USD");
        when(investmentRepository.findAll()).thenReturn(List.of(inv1));

        failingService.refreshRates();

        // Should not have attempted to save anything due to top-level batch catch
        verify(fxRateRepository, never()).save(any());
    }
}
