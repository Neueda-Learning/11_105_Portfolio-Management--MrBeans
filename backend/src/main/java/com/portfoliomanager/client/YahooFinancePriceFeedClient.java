package com.portfoliomanager.client;

import com.portfoliomanager.util.CalendarConverter;
import com.portfoliomanager.util.MoneyMath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(name = "portfolio.price-feed.provider", havingValue = "yahoo-finance", matchIfMissing = true)
public class YahooFinancePriceFeedClient implements PriceFeedClient {

    private static final Logger log = LoggerFactory.getLogger(YahooFinancePriceFeedClient.class);

    @Override
    public Map<String, CurrentPrice> getPrices(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Map.of();
        }

        try {
            // Batch call as strictly required by Section 3.3
            String[] tickerArray = symbols.toArray(new String[0]);
            Map<String, Stock> stocks = YahooFinance.get(tickerArray);
            
            Map<String, CurrentPrice> results = new HashMap<>();
            
            if (stocks != null) {
                for (Map.Entry<String, Stock> entry : stocks.entrySet()) {
                    Stock stock = entry.getValue();
                    if (stock != null && stock.getQuote() != null && stock.getQuote().getPrice() != null) {
                        
                        BigDecimal price = MoneyMath.roundRate(stock.getQuote().getPrice());
                        String currency = stock.getCurrency() != null ? stock.getCurrency() : "USD"; // Default fallback
                        
                        // Calendar conversion strictly at this boundary
                        Instant fetchedAt = Instant.now();
                        if (stock.getQuote().getLastTradeTime() != null) {
                            fetchedAt = CalendarConverter.toInstant(stock.getQuote().getLastTradeTime());
                        }
                        
                        results.put(entry.getKey(), new CurrentPrice(price, currency, fetchedAt));
                    } else {
                        log.warn("Missing quote data from Yahoo for symbol: {}", entry.getKey());
                    }
                }
            }
            return results;
        } catch (IOException e) {
            log.error("Failed to fetch batch prices from Yahoo Finance API", e);
            throw new RuntimeException("Yahoo Finance API request failed", e);
        }
    }
}
