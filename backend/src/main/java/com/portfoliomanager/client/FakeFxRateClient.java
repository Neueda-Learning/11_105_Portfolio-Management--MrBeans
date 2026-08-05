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
        hardcodedRates.put("USDINR=X", new CurrentRate(new BigDecimal("83.50000000")));
        hardcodedRates.put("EURINR=X", new CurrentRate(new BigDecimal("90.10000000")));
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
