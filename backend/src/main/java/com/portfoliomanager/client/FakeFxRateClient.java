package com.portfoliomanager.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "portfolio.price-feed.provider", havingValue = "fake")
public class FakeFxRateClient implements FxRateClient {

    private final Map<String, CurrentRate> hardcodedRates = new HashMap<>();

    public FakeFxRateClient() {
        // Hardcoded rates: CURRENCY to USD
        hardcodedRates.put("EURUSD=X", new CurrentRate(new BigDecimal("1.09000000")));
        hardcodedRates.put("GBPUSD=X", new CurrentRate(new BigDecimal("1.27000000")));
        hardcodedRates.put("JPYUSD=X", new CurrentRate(new BigDecimal("0.00645000")));
        hardcodedRates.put("CADUSD=X", new CurrentRate(new BigDecimal("0.73000000")));
        hardcodedRates.put("AUDUSD=X", new CurrentRate(new BigDecimal("0.65000000")));
        hardcodedRates.put("INRUSD=X", new CurrentRate(new BigDecimal("0.01197600")));
    }

    @Override
    public Map<String, CurrentRate> getRates(List<String> pairs) {
        Map<String, CurrentRate> results = new HashMap<>();
        for (String pair : pairs) {
            if (hardcodedRates.containsKey(pair)) {
                results.put(pair, hardcodedRates.get(pair));
            }
        }
        return results;
    }

    public void setHardcodedRate(String pair, BigDecimal rate) {
        hardcodedRates.put(pair, new CurrentRate(rate));
    }
}
