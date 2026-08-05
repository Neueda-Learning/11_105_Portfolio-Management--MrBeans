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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FxRateService {

    private static final Logger log = LoggerFactory.getLogger(FxRateService.class);

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

    /**
     * Cache lookup for a specific date. 
     * If from and to currency are identical, returns 1.0.
     */
    @Transactional(readOnly = true)
    public Optional<BigDecimal> getRate(String fromCurrency, String toCurrency, LocalDate date) {
        if (fromCurrency.equalsIgnoreCase(toCurrency)) {
            return Optional.of(BigDecimal.ONE);
        }
        return fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(fromCurrency, toCurrency, date)
                .map(FxRate::getRate);
    }

    /**
     * Scheduled job to refresh FX rates daily/hourly based on configuration.
     * Looks up distinct currencies from investments and fetches rates to the home currency.
     */
    @Scheduled(fixedRateString = "${portfolio.update-frequency-ms:3600000}")
    @Transactional
    public void refreshRates() {
        log.info("Starting scheduled FX rate refresh cycle");

        // Hardcoding home currency to INR for MVP as per default settings in V2 migration,
        // but ideally this would be fetched from UserSettingsService.
        String homeCurrency = "INR"; 
        LocalDate today = LocalDate.now();

        List<Investment> investments = investmentRepository.findAll();
        if (investments.isEmpty()) {
            log.info("No investments found, skipping FX refresh");
            return;
        }

        // Get distinct foreign currencies
        Set<String> foreignCurrencies = investments.stream()
                .map(Investment::getCurrency)
                .filter(currency -> !currency.equalsIgnoreCase(homeCurrency))
                .collect(Collectors.toSet());

        if (foreignCurrencies.isEmpty()) {
            log.info("All investments are in home currency, no FX fetch needed");
            return;
        }

        // Build Yahoo Finance FX pairs (e.g., "USDINR=X")
        List<String> pairs = foreignCurrencies.stream()
                .map(foreign -> foreign + homeCurrency + "=X")
                .collect(Collectors.toList());

        Map<String, FxRateClient.CurrentRate> rates;
        try {
            rates = fxRateClient.getRates(pairs);
        } catch (Exception e) {
            log.error("Failed to fetch batched FX rates from provider. Skipping this refresh cycle.", e);
            return;
        }

        // Graceful per-pair degradation
        for (String foreignCurrency : foreignCurrencies) {
            String pair = foreignCurrency + homeCurrency + "=X";
            try {
                FxRateClient.CurrentRate currentRate = rates.get(pair);
                if (currentRate == null || currentRate.rate() == null) {
                    log.warn("No FX rate returned for pair: {}. Skipping.", pair);
                    continue;
                }

                saveOrUpdateRate(foreignCurrency, homeCurrency, today, currentRate.rate());
            } catch (Exception e) {
                log.error("Failed to process and save FX rate for pair: {}", pair, e);
            }
        }

        log.info("Completed scheduled FX rate refresh cycle");
    }

    private void saveOrUpdateRate(String from, String to, LocalDate date, BigDecimal rateValue) {
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
