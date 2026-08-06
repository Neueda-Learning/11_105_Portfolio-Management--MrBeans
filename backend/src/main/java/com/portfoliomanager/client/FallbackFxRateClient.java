package com.portfoliomanager.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Composite client that tries Yahoo Finance first.
 * If Yahoo fails (e.g., HTTP 429 Rate Limit), it seamlessly falls back to the Frankfurter API.
 */
@Component
@Primary
@ConditionalOnProperty(name = "portfolio.price-feed.provider", havingValue = "yahoo-finance", matchIfMissing = true)
public class FallbackFxRateClient implements FxRateClient {

    private static final Logger log = LoggerFactory.getLogger(FallbackFxRateClient.class);

    // Instantiate directly to avoid Spring circular dependency issues with FxRateClient beans
    private final FxRateClient primary = new YahooFinanceFxRateClient();
    private final FxRateClient fallback = new FrankfurterFxRateClient();

    @Override
    public Map<String, CurrentRate> getRates(List<String> pairs) {
        try {
            Map<String, CurrentRate> primaryRates = primary.getRates(pairs);
            if (primaryRates != null && !primaryRates.isEmpty()) {
                return primaryRates;
            }
        } catch (Exception e) {
            log.warn("Primary FX client (Yahoo) failed: {}. Falling back to secondary...", e.getMessage());
        }

        log.info("Delegating FX rate fetch to fallback client (Frankfurter)");
        try {
            return fallback.getRates(pairs);
        } catch (Exception e) {
            log.error("Fallback FX client (Frankfurter) also failed: {}", e.getMessage());
            return new HashMap<>();
        }
    }
}
