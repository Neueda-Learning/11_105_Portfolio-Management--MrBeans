package com.portfoliomanager.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "portfolio.price-feed.provider", havingValue = "fake")
public class FakePriceFeedClient implements PriceFeedClient {

    private final Map<String, CurrentPrice> hardcodedPrices = new HashMap<>();

    public FakePriceFeedClient() {
        hardcodedPrices.put("AAPL", new CurrentPrice(new BigDecimal("150.0000"), "USD", Instant.now()));
        hardcodedPrices.put("MSFT", new CurrentPrice(new BigDecimal("300.0000"), "USD", Instant.now()));
        hardcodedPrices.put("VTI", new CurrentPrice(new BigDecimal("220.0000"), "USD", Instant.now()));
    }

    @Override
    public Map<String, CurrentPrice> getPrices(List<String> symbols) {
        Map<String, CurrentPrice> results = new HashMap<>();
        for (String symbol : symbols) {
            if (hardcodedPrices.containsKey(symbol)) {
                results.put(symbol, hardcodedPrices.get(symbol));
            } else {
                // Simulate an unknown ticker returning nothing, as real Yahoo would
            }
        }
        return results;
    }
    
    // Helper for testing to inject specific scenarios
    public void setHardcodedPrice(String symbol, BigDecimal price, String currency) {
        hardcodedPrices.put(symbol, new CurrentPrice(price, currency, Instant.now()));
    }
}
