package com.portfoliomanager.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfoliomanager.dto.instrument.InstrumentSearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Component
public class YahooFinanceSearchClient {

    private static final Logger log = LoggerFactory.getLogger(YahooFinanceSearchClient.class);
    private static final String SEARCH_URL = "https://query1.finance.yahoo.com/v1/finance/search?q=%s&lang=en-US&region=US&quotesCount=10&newsCount=0&enableFuzzyQuery=false&enableCb=false";

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();
    private final ObjectMapper mapper = new ObjectMapper();

    public List<InstrumentSearchResult> search(String query) {
        List<InstrumentSearchResult> results = new ArrayList<>();
        try {
            String url = String.format(SEARCH_URL, java.net.URLEncoder.encode(query, "UTF-8"));
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("Yahoo Finance search returned HTTP {}", response.statusCode());
                return results;
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode quotes = root.path("quotes");
            if (quotes.isMissingNode() || !quotes.isArray()) return results;

            for (JsonNode q : quotes) {
                String symbol   = text(q, "symbol");
                String name     = firstNonEmpty(text(q, "longname"), text(q, "shortname"));
                String quoteType = text(q, "quoteType");
                String exchange = firstNonEmpty(text(q, "exchDisp"), text(q, "exchange"));
                String currency = text(q, "currency");

                if (symbol.isBlank()) continue;
                results.add(new InstrumentSearchResult(symbol, name, mapType(quoteType), exchange, currency));
            }
        } catch (Exception e) {
            log.error("Instrument search failed for query '{}': {}", query, e.getMessage());
        }
        return results;
    }

    private String text(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return (n != null && !n.isNull()) ? n.asText("") : "";
    }

    private String firstNonEmpty(String... values) {
        for (String v : values) if (v != null && !v.isBlank()) return v;
        return "";
    }

    private String mapType(String quoteType) {
        if (quoteType == null) return "OTHER";
        return switch (quoteType.toUpperCase()) {
            case "EQUITY"         -> "STOCK";
            case "CRYPTOCURRENCY" -> "CRYPTO";
            case "ETF"            -> "ETF";
            case "MUTUALFUND"     -> "MUTUAL_FUND";
            case "BOND", "FUTURE" -> "BOND";
            case "CURRENCY"       -> "CASH";
            default               -> "OTHER";
        };
    }
}
