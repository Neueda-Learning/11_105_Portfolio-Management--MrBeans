package com.portfoliomanager.client;

import com.portfoliomanager.util.MoneyMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "portfolio.price-feed.provider", havingValue = "yahoo-finance", matchIfMissing = true)
public class YahooFinanceFxRateClient implements FxRateClient {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceFxRateClient.class);

    @Override
    public Map<String, CurrentRate> getRates(List<String> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            return Map.of();
        }

        try {
            // Note: yahoofinance-api typically fetches FX via YahooFinance.getFx() for a single string,
            // but the PRD dictates YahooFinance.getFx(String[]) to enforce batching semantics.
            // Under the hood in many wrapper variants, getting FX in batch is just calling get()
            // with the "=X" suffixed strings since FX rates are treated as pseudo-stocks.
            String[] pairArray = pairs.toArray(new String[0]);
            
            // To ensure compatibility with standard yahoofinance-api 3.x, we fetch the batch
            // as standard stocks. Yahoo models "EURUSD=X" as a Stock object containing the rate as its price.
            Map<String, Stock> quotes = YahooFinance.get(pairArray);
            
            Map<String, CurrentRate> results = new HashMap<>();
            
            if (quotes != null) {
                for (Map.Entry<String, Stock> entry : quotes.entrySet()) {
                    Stock stock = entry.getValue();
                    if (stock != null && stock.getQuote() != null && stock.getQuote().getPrice() != null) {
                        BigDecimal rate = MoneyMath.roundQuantity(stock.getQuote().getPrice()); // Scale 8 for precise FX rates
                        results.put(entry.getKey(), new CurrentRate(rate));
                    } else {
                        log.warn("Missing FX quote data from Yahoo for pair: {}", entry.getKey());
                    }
                }
            }
            return results;
        } catch (IOException e) {
            if (isRateLimited(e)) {
                log.warn("Yahoo Finance FX API rate-limited (HTTP 429). Skipping this cycle.");
                return Map.of();
            }
            log.error("Failed to fetch batch FX rates from Yahoo Finance API", e);
            throw new RuntimeException("Yahoo Finance API FX request failed", e);
        }
    }

    private boolean isRateLimited(IOException e) {
        String msg = e.getMessage();
        return msg != null && msg.contains("HTTP response code: 429");
    }
}
