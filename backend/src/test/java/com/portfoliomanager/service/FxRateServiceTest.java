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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
        fakeFxRateClient = new FakeFxRateClient();
        fxRateService = new FxRateService(fxRateRepository, investmentRepository, fakeFxRateClient);
    }

    // ── getRate ──────────────────────────────────────────────────────────

    @Test
    void getRate_SameCurrency_ReturnsOne() {
        Optional<BigDecimal> rate = fxRateService.getRate("USD", "USD", LocalDate.now());
        assertTrue(rate.isPresent());
        assertEquals(BigDecimal.ONE, rate.get());
        verify(fxRateRepository, never()).findByFromCurrencyAndToCurrencyAndRateDate(anyString(), anyString(), any());
    }

    @Test
    void getRate_DirectHit_ReturnsFromDb() {
        LocalDate today = LocalDate.now();
        FxRate fx = fxRate("EUR", "USD", today, "1.09");
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("EUR", "USD", today))
                .thenReturn(Optional.of(fx));

        Optional<BigDecimal> rate = fxRateService.getRate("EUR", "USD", today);
        assertTrue(rate.isPresent());
        assertEquals(new BigDecimal("1.09"), rate.get());
    }

    @Test
    void getRate_InverseHit_ReturnsInverted() {
        LocalDate today = LocalDate.now();
        // No direct EUR→INR
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("EUR", "INR", today))
                .thenReturn(Optional.empty());
        // But INR→EUR exists (the "from" param in invertedRate is toCurrency, to is fromCurrency)
        FxRate inverse = fxRate("INR", "EUR", today, "0.01");
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("INR", "EUR", today))
                .thenReturn(Optional.of(inverse));

        Optional<BigDecimal> rate = fxRateService.getRate("EUR", "INR", today);
        assertTrue(rate.isPresent());
        // 1 / 0.01 = 100
        assertEquals(0, new BigDecimal("100.00000000").compareTo(rate.get()));
    }

    @Test
    void getRate_CrossRateViaUsd_Works() {
        LocalDate today = LocalDate.now();
        // No direct or inverse for EUR→INR
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("EUR", "INR", today))
                .thenReturn(Optional.empty());
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("INR", "EUR", today))
                .thenReturn(Optional.empty());
        // But cross-rate via USD: EUR→USD = 1.09, INR→USD = 0.012
        FxRate eurUsd = fxRate("EUR", "USD", today, "1.09");
        FxRate inrUsd = fxRate("INR", "USD", today, "0.012");
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("EUR", "USD", today))
                .thenReturn(Optional.of(eurUsd));
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("INR", "USD", today))
                .thenReturn(Optional.of(inrUsd));

        Optional<BigDecimal> rate = fxRateService.getRate("EUR", "INR", today);
        assertTrue(rate.isPresent());
        // 1.09 / 0.012 ≈ 90.83333333
        BigDecimal expected = new BigDecimal("1.09").divide(new BigDecimal("0.012"), 8, RoundingMode.HALF_UP);
        assertEquals(expected, rate.get());
    }

    @Test
    void getRate_CrossRateViaUsdToFrom_Works() {
        LocalDate today = LocalDate.now();
        // No direct, inverse, or from→USD cross-rate
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(eq("EUR"), eq("INR"), eq(today)))
                .thenReturn(Optional.empty());
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(eq("INR"), eq("EUR"), eq(today)))
                .thenReturn(Optional.empty());
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(eq("EUR"), eq("USD"), eq(today)))
                .thenReturn(Optional.empty());
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(eq("INR"), eq("USD"), eq(today)))
                .thenReturn(Optional.empty());
        // But USD→EUR and USD→INR exist
        FxRate usdEur = fxRate("USD", "EUR", today, "0.92");
        FxRate usdInr = fxRate("USD", "INR", today, "83.50");
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("USD", "EUR", today))
                .thenReturn(Optional.of(usdEur));
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("USD", "INR", today))
                .thenReturn(Optional.of(usdInr));

        Optional<BigDecimal> rate = fxRateService.getRate("EUR", "INR", today);
        assertTrue(rate.isPresent());
    }

    @Test
    void getRate_FallsBackToMostRecent() {
        LocalDate today = LocalDate.now();
        // All exact-date lookups return empty
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(anyString(), anyString(), eq(today)))
                .thenReturn(Optional.empty());
        // Most recent direct rate exists
        FxRate recent = fxRate("EUR", "INR", today.minusDays(5), "90.00");
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency("EUR", "INR"))
                .thenReturn(Optional.of(recent));

        Optional<BigDecimal> rate = fxRateService.getRate("EUR", "INR", today);
        assertTrue(rate.isPresent());
        assertEquals(new BigDecimal("90.00"), rate.get());
    }

    @Test
    void getRate_OnDemandLiveFetch_Works() {
        LocalDate today = LocalDate.now();
        // All lookups empty
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(anyString(), anyString(), any()))
                .thenReturn(Optional.empty());
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency(anyString(), anyString()))
                .thenReturn(Optional.empty());

        // FakeFxRateClient provides EURUSD=X
        Optional<BigDecimal> rate = fxRateService.getRate("EUR", "USD", today);
        assertTrue(rate.isPresent());
        // Should have saved it
        verify(fxRateRepository, atLeastOnce()).save(any(FxRate.class));
    }

    @Test
    void getRate_AllFail_ReturnsEmpty() {
        LocalDate today = LocalDate.now();
        FxRateClient failingClient = mock(FxRateClient.class);
        when(failingClient.getRates(anyList())).thenReturn(Map.of());
        FxRateService service = new FxRateService(fxRateRepository, investmentRepository, failingClient);

        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(anyString(), anyString(), any()))
                .thenReturn(Optional.empty());
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency(anyString(), anyString()))
                .thenReturn(Optional.empty());

        Optional<BigDecimal> rate = service.getRate("XYZ", "ABC", today);
        assertTrue(rate.isEmpty());
    }

    // ── getLatestRate ───────────────────────────────────────────────────

    @Test
    void getLatestRate_SameCurrency_ReturnsOne() {
        Optional<BigDecimal> rate = fxRateService.getLatestRate("EUR", "EUR");
        assertTrue(rate.isPresent());
        assertEquals(BigDecimal.ONE, rate.get());
    }

    @Test
    void getLatestRate_DirectHit() {
        FxRate fx = fxRate("EUR", "USD", LocalDate.now(), "1.09");
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency("EUR", "USD"))
                .thenReturn(Optional.of(fx));

        Optional<BigDecimal> rate = fxRateService.getLatestRate("EUR", "USD");
        assertTrue(rate.isPresent());
        assertEquals(new BigDecimal("1.09"), rate.get());
    }

    @Test
    void getLatestRate_InverseHit() {
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency("EUR", "INR"))
                .thenReturn(Optional.empty());
        FxRate inv = fxRate("INR", "EUR", LocalDate.now(), "0.011");
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency("INR", "EUR"))
                .thenReturn(Optional.of(inv));

        Optional<BigDecimal> rate = fxRateService.getLatestRate("EUR", "INR");
        assertTrue(rate.isPresent());
    }

    @Test
    void getLatestRate_CrossRate() {
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency("EUR", "INR"))
                .thenReturn(Optional.empty());
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency("INR", "EUR"))
                .thenReturn(Optional.empty());
        // Cross via USD
        FxRate eurUsd = fxRate("EUR", "USD", LocalDate.now(), "1.09");
        FxRate inrUsd = fxRate("INR", "USD", LocalDate.now(), "0.012");
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency("EUR", "USD"))
                .thenReturn(Optional.of(eurUsd));
        when(fxRateRepository.findMostRecentByFromCurrencyAndToCurrency("INR", "USD"))
                .thenReturn(Optional.of(inrUsd));

        Optional<BigDecimal> rate = fxRateService.getLatestRate("EUR", "INR");
        assertTrue(rate.isPresent());
    }

    // ── saveOrUpdateRate ────────────────────────────────────────────────

    @Test
    void saveOrUpdateRate_ExistingRecord_Updates() {
        LocalDate today = LocalDate.now();
        FxRate existing = fxRate("EUR", "USD", today, "1.08");
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("EUR", "USD", today))
                .thenReturn(Optional.of(existing));

        fxRateService.saveOrUpdateRate("EUR", "USD", today, new BigDecimal("1.10"));

        assertEquals(new BigDecimal("1.10"), existing.getRate());
        verify(fxRateRepository).save(existing);
    }

    @Test
    void saveOrUpdateRate_NewRecord_Creates() {
        LocalDate today = LocalDate.now();
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate("EUR", "USD", today))
                .thenReturn(Optional.empty());

        fxRateService.saveOrUpdateRate("EUR", "USD", today, new BigDecimal("1.10"));

        ArgumentCaptor<FxRate> captor = ArgumentCaptor.forClass(FxRate.class);
        verify(fxRateRepository).save(captor.capture());
        assertEquals("EUR", captor.getValue().getFromCurrency());
        assertEquals("USD", captor.getValue().getToCurrency());
        assertEquals(new BigDecimal("1.10"), captor.getValue().getRate());
    }

    // ── refreshRates ────────────────────────────────────────────────────

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

        // 6 supported pairs * 2 (direct+inverse) = 12 saves
        verify(fxRateRepository, times(12)).save(any(FxRate.class));
    }

    @Test
    void refreshRates_SkipsInvalidPairGracefully() {
        Investment validInv = new Investment();
        validInv.setCurrency("USD");
        Investment invalidInv = new Investment();
        invalidInv.setCurrency("ZAR");

        when(investmentRepository.findAll()).thenReturn(List.of(validInv, invalidInv));
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(anyString(), anyString(), any()))
                .thenReturn(Optional.empty());

        fxRateService.refreshRates();
        verify(fxRateRepository, times(12)).save(any(FxRate.class));
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
        verify(fxRateRepository, never()).save(any());
    }

    @Test
    void refreshRates_EmptyPairs_SkipsGracefully() {
        // Only USD investments → no non-USD pairs to fetch
        Investment inv = new Investment();
        inv.setCurrency("USD");
        when(investmentRepository.findAll()).thenReturn(List.of(inv));
        when(fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(anyString(), anyString(), any()))
                .thenReturn(Optional.empty());

        fxRateService.refreshRates();
        // Still saves for the other 6 supported home currencies (EUR, GBP, JPY, CAD, AUD, INR)
        verify(fxRateRepository, times(12)).save(any(FxRate.class));
    }

    // ── helpers ──────────────────────────────────────────────────────────

    private FxRate fxRate(String from, String to, LocalDate date, String rate) {
        FxRate fx = new FxRate();
        fx.setId(UUID.randomUUID());
        fx.setFromCurrency(from);
        fx.setToCurrency(to);
        fx.setRateDate(date);
        fx.setRate(new BigDecimal(rate));
        return fx;
    }
}
