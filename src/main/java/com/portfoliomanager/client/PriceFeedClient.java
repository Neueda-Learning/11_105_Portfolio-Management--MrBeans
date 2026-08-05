package com.portfoliomanager.client;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public interface PriceFeedClient {

    /**
     * Fetches real-time prices for a batch of symbols.
     *
     * @param symbols list of ticker symbols
     * @return a map of symbol to its current price data
     */
    Map<String, CurrentPrice> getPrices(List<String> symbols);

    record CurrentPrice(BigDecimal price, String currency, Instant fetchedAt) {}
}
