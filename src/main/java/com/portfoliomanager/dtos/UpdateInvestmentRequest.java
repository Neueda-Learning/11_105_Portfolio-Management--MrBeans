package com.portfoliomanager.dtos;

import com.portfoliomanager.entity.InvestmentType;
import java.util.Map;

public class UpdateInvestmentRequest {
    private String symbol;
    private String name;
    private InvestmentType type;
    private String currency;
    private Map<String, Object> metadata;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public InvestmentType getType() { return type; }
    public void setType(InvestmentType type) { this.type = type; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public Map<String, Object> getMetadata() { return metadata; }
    public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
}
