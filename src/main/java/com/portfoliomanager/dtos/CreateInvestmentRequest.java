package com.portfoliomanager.dtos;

import com.portfoliomanager.entity.InvestmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public class CreateInvestmentRequest {
    @NotBlank
    private String symbol;

    private String name;

    @NotNull
    private InvestmentType type;

    @NotBlank
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
