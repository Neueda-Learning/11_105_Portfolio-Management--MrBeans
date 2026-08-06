package com.portfoliomanager.dto.instrument;

public class InstrumentSearchResult {
    private String symbol;
    private String name;
    private String type;       // STOCK, CRYPTO, ETF, BOND, etc.
    private String exchange;
    private String currency;

    public InstrumentSearchResult() {}

    public InstrumentSearchResult(String symbol, String name, String type, String exchange, String currency) {
        this.symbol = symbol;
        this.name = name;
        this.type = type;
        this.exchange = exchange;
        this.currency = currency;
    }

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getExchange() { return exchange; }
    public void setExchange(String exchange) { this.exchange = exchange; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
