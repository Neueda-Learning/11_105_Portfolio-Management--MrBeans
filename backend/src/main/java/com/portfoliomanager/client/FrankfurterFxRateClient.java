package com.portfoliomanager.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "portfolio.price-feed.provider", havingValue = "frankfurter")
public class FrankfurterFxRateClient implements FxRateClient {

    private static final Logger log = LoggerFactory.getLogger(FrankfurterFxRateClient.class);
    private static final String BASE_URL = "https://api.frankfurter.app/latest?from={base}&to={quote}";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public Map<String, CurrentRate> getRates(List<String> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            return Map.of();
        }

        Map<String, CurrentRate> results = new HashMap<>();

        for (String pair : pairs) {
            try {
                // Parse Yahoo-style pair (e.g., "EURUSD=X")
                if (pair.length() < 6) continue;
                String base = pair.substring(0, 3).toUpperCase();
                String quote = pair.substring(3, 6).toUpperCase();

                // Frankfurter API doesn't support USD->USD, it returns an error.
                if (base.equals(quote)) {
                    results.put(pair, new CurrentRate(BigDecimal.ONE));
                    continue;
                }

                FrankfurterResponse response = restTemplate.getForObject(
                        BASE_URL, FrankfurterResponse.class, base, quote);

                if (response != null && response.rates != null && response.rates.containsKey(quote)) {
                    BigDecimal rate = response.rates.get(quote);
                    results.put(pair, new CurrentRate(rate));
                } else {
                    log.warn("Frankfurter API did not return rate for {}->{}", base, quote);
                }

            } catch (Exception e) {
                log.warn("Failed to fetch rate from Frankfurter for pair {}: {}", pair, e.getMessage());
            }
        }

        return results;
    }

    private static class FrankfurterResponse {
        public Map<String, BigDecimal> rates;
    }
}
