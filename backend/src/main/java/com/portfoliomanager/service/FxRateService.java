package com.portfoliomanager.service;

import com.portfoliomanager.repository.FxRateRepository;
import com.portfoliomanager.client.FxRateClient;
import com.portfoliomanager.model.FxRate;

import com.portfoliomanager.model.Investment;
import com.portfoliomanager.repository.InvestmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FxRateService {

    private static final Logger log = LoggerFactory.getLogger(FxRateService.class);

    /** All currencies the user can select as base currency. */
    private static final Set<String> SUPPORTED_HOME_CURRENCIES =
            Set.of("USD", "EUR", "GBP", "JPY", "CAD", "AUD", "INR");

    /** Universal pivot currency used for cross-rate computation. */
    private static final String PIVOT = "USD";

    private final FxRateRepository fxRateRepository;
    private final InvestmentRepository investmentRepository;
    private final FxRateClient fxRateClient;

    public FxRateService(FxRateRepository fxRateRepository,
                         InvestmentRepository investmentRepository,
                         FxRateClient fxRateClient) {
        this.fxRateRepository = fxRateRepository;
        this.investmentRepository = investmentRepository;
        this.fxRateClient = fxRateClient;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Returns the exchange rate FROM → TO for the given date.
     * Resolution order:
     *   1. Same-currency shortcut → 1.0
     *   2. Direct DB hit for that exact date
     *   3. Inverse rate (TO→FROM) for that exact date
     *   4. Cross-rate via USD pivot for that exact date
     *   5. Same cascade using the most-recently stored rate (any date)
     *   6. On-demand live API fetch + persist
     *   7. Fallback 1.0 (logged as warning)
     */
    @Transactional
    public Optional<BigDecimal> getRate(String fromCurrency, String toCurrency, LocalDate date) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return Optional.of(BigDecimal.ONE);
        }

        // 1. Exact date, direct
        Optional<BigDecimal> direct = fxRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(fromCurrency, toCurrency, date)
                .map(FxRate::getRate);
        if (direct.isPresent()) return direct;

        // 2. Exact date, inverse
        Optional<BigDecimal> inverse = invertedRate(toCurrency, fromCurrency, date);
        if (inverse.isPresent()) return inverse;

        // 3. Exact date, cross-rate via USD
        Optional<BigDecimal> cross = crossRateViaUsd(fromCurrency, toCurrency, date);
        if (cross.isPresent()) return cross;

        // 4. Most-recent (any date), same cascade
        Optional<BigDecimal> recent = getLatestRate(fromCurrency, toCurrency);
        if (recent.isPresent()) return recent;

        // 5. On-demand live fetch
        BigDecimal live = fetchLiveRate(fromCurrency, toCurrency);
        if (live != null) {
            saveOrUpdateRate(fromCurrency, toCurrency, LocalDate.now(), live);
            return Optional.of(live);
        }

        log.warn("No FX rate found for {}->{} on {}. Using 1.0 as fallback.", fromCurrency, toCurrency, date);
        return Optional.empty();
    }

    /**
     * Returns the most-recently stored FX rate for FROM → TO (any date).
     * Ideal for trend calculations where per-date granularity is unnecessary
     * and calling the live API per date would cause rate-limiting.
     *
     * Resolution: direct → inverse → cross-rate via USD pivot.
     * Falls back to a single on-demand live fetch if nothing is stored.
     */
    @Transactional
    public Optional<BigDecimal> getLatestRate(String fromCurrency, String toCurrency) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return Optional.of(BigDecimal.ONE);
        }

        // Direct
        Optional<BigDecimal> recentDirect = fxRateRepository
                .findMostRecentByFromCurrencyAndToCurrency(fromCurrency, toCurrency)
                .map(FxRate::getRate);
        if (recentDirect.isPresent()) return recentDirect;

        // Inverse
        Optional<BigDecimal> recentInverse = fxRateRepository
                .findMostRecentByFromCurrencyAndToCurrency(toCurrency, fromCurrency)
                .map(FxRate::getRate)
                .filter(r -> r.compareTo(BigDecimal.ZERO) > 0)
                .map(r -> BigDecimal.ONE.divide(r, 8, RoundingMode.HALF_UP));
        if (recentInverse.isPresent()) return recentInverse;

        // Cross-rate
        Optional<BigDecimal> recentCross = crossRateMostRecent(fromCurrency, toCurrency);
        if (recentCross.isPresent()) return recentCross;

        // Single on-demand live fetch (one API call, not per-date)
        BigDecimal live = fetchLiveRate(fromCurrency, toCurrency);
        if (live != null) {
            saveOrUpdateRate(fromCurrency, toCurrency, LocalDate.now(), live);
            return Optional.of(live);
        }

        log.warn("No FX rate found for {}->{}. Using 1.0 as fallback.", fromCurrency, toCurrency);
        return Optional.empty();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Scheduled refresh
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Fetches today's rate for every (investmentCurrency → USD) and every
     * (supportedHomeCurrency → USD) pair.  Storing everything relative to USD
     * lets getRate() derive any arbitrary cross-rate without additional API calls.
     */
    @Scheduled(fixedRateString = "${portfolio.update-frequency-ms:3600000}")
    @Transactional
    public void refreshRates() {
        log.info("Starting FX rate refresh cycle");
        LocalDate today = LocalDate.now();

        // Collect all currencies we care about:
        // ALL supported home currencies + all investment currencies
        Set<String> allCurrencies = new HashSet<>(SUPPORTED_HOME_CURRENCIES);
        investmentRepository.findAll().stream()
                .map(Investment::getCurrency)
                .filter(Objects::nonNull)
                .forEach(allCurrencies::add);

        // Build Yahoo Finance pairs: CURRENCY→USD  (e.g. "JPYUSD=X", "EURUSD=X")
        // This ensures we have rates for every supported home currency,
        // not just the currencies of current investments.
        List<String> pairs = allCurrencies.stream()
                .filter(c -> !c.equalsIgnoreCase(PIVOT))
                .map(c -> c + PIVOT + "=X")
                .collect(Collectors.toList());

        if (pairs.isEmpty()) {
            log.info("No FX pairs to fetch");
            return;
        }

        Map<String, FxRateClient.CurrentRate> rates;
        try {
            rates = fxRateClient.getRates(pairs);
        } catch (Exception e) {
            log.error("Failed to fetch FX rates from provider", e);
            return;
        }

        int stored = 0;
        for (String currency : allCurrencies) {
            if (currency.equalsIgnoreCase(PIVOT)) continue;
            String pair = currency + PIVOT + "=X";
            try {
                FxRateClient.CurrentRate cr = rates.get(pair);
                if (cr == null || cr.rate() == null) {
                    log.warn("No rate for pair {}", pair);
                    continue;
                }
                // Store CURRENCY→USD
                saveOrUpdateRate(currency, PIVOT, today, cr.rate());
                // Also store USD→CURRENCY (inverse) for convenience
                if (cr.rate().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal inv = BigDecimal.ONE.divide(cr.rate(), 8, RoundingMode.HALF_UP);
                    saveOrUpdateRate(PIVOT, currency, today, inv);
                }
                stored++;
            } catch (Exception e) {
                log.error("Failed to process FX rate for pair {}", pair, e);
            }
        }
        log.info("FX rate refresh complete — stored {} currency pairs", stored);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Private helpers
    // ────────────────────────────────────────────────────────────────────────

    private Optional<BigDecimal> invertedRate(String from, String to, LocalDate date) {
        return fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(from, to, date)
                .map(FxRate::getRate)
                .filter(r -> r.compareTo(BigDecimal.ZERO) > 0)
                .map(r -> BigDecimal.ONE.divide(r, 8, RoundingMode.HALF_UP));
    }

    private Optional<BigDecimal> crossRateViaUsd(String from, String to, LocalDate date) {
        // from→USD / to→USD  = from→to
        Optional<BigDecimal> fromUsd = fxRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(from, PIVOT, date).map(FxRate::getRate);
        Optional<BigDecimal> toUsd = fxRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(to, PIVOT, date).map(FxRate::getRate);

        if (fromUsd.isPresent() && toUsd.isPresent() && toUsd.get().compareTo(BigDecimal.ZERO) > 0) {
            return Optional.of(fromUsd.get().divide(toUsd.get(), 8, RoundingMode.HALF_UP));
        }

        // Also try USD→from and USD→to
        Optional<BigDecimal> usdFrom = fxRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(PIVOT, from, date).map(FxRate::getRate);
        Optional<BigDecimal> usdTo = fxRateRepository
                .findByFromCurrencyAndToCurrencyAndRateDate(PIVOT, to, date).map(FxRate::getRate);

        if (usdFrom.isPresent() && usdTo.isPresent() && usdFrom.get().compareTo(BigDecimal.ZERO) > 0) {
            return Optional.of(usdTo.get().divide(usdFrom.get(), 8, RoundingMode.HALF_UP));
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> crossRateMostRecent(String from, String to) {
        Optional<BigDecimal> fromUsd = fxRateRepository
                .findMostRecentByFromCurrencyAndToCurrency(from, PIVOT).map(FxRate::getRate);
        Optional<BigDecimal> toUsd = fxRateRepository
                .findMostRecentByFromCurrencyAndToCurrency(to, PIVOT).map(FxRate::getRate);
        if (fromUsd.isPresent() && toUsd.isPresent() && toUsd.get().compareTo(BigDecimal.ZERO) > 0) {
            return Optional.of(fromUsd.get().divide(toUsd.get(), 8, RoundingMode.HALF_UP));
        }
        Optional<BigDecimal> usdFrom = fxRateRepository
                .findMostRecentByFromCurrencyAndToCurrency(PIVOT, from).map(FxRate::getRate);
        Optional<BigDecimal> usdTo = fxRateRepository
                .findMostRecentByFromCurrencyAndToCurrency(PIVOT, to).map(FxRate::getRate);
        if (usdFrom.isPresent() && usdTo.isPresent() && usdFrom.get().compareTo(BigDecimal.ZERO) > 0) {
            return Optional.of(usdTo.get().divide(usdFrom.get(), 8, RoundingMode.HALF_UP));
        }
        return Optional.empty();
    }

    /** Fetches a single rate live from the API without persisting it. Returns null on failure. */
    private BigDecimal fetchLiveRate(String from, String to) {
        String pair = from + to + "=X";
        try {
            Map<String, FxRateClient.CurrentRate> rates = fxRateClient.getRates(List.of(pair));
            FxRateClient.CurrentRate cr = rates.get(pair);
            if (cr != null && cr.rate() != null) {
                log.info("On-demand FX fetch: {} = {}", pair, cr.rate());
                return cr.rate();
            }
        } catch (Exception e) {
            log.warn("On-demand FX fetch failed for {}: {}", pair, e.getMessage());
        }
        return null;
    }

    void saveOrUpdateRate(String from, String to, LocalDate date, BigDecimal rateValue) {
        Optional<FxRate> existing = fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(from, to, date);
        if (existing.isPresent()) {
            FxRate rate = existing.get();
            rate.setRate(rateValue);
            fxRateRepository.save(rate);
        } else {
            FxRate rate = new FxRate();
            rate.setFromCurrency(from);
            rate.setToCurrency(to);
            rate.setRateDate(date);
            rate.setRate(rateValue);
            fxRateRepository.save(rate);
        }
    }
}
