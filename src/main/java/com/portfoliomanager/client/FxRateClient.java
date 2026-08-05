package com.portfoliomanager.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface FxRateClient {

    /**
     * Fetches current exchange rates for a batch of currency pairs.
     * The pairs are formatted as expected by the provider (e.g. "USDINR=X").
     */
    Map<String, CurrentRate> getRates(List<String> pairs);

    record CurrentRate(BigDecimal rate) {}
}
