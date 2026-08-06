package com.portfoliomanager.dto.investment;

import com.portfoliomanager.model.InvestmentType;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.Map;

public class UpdateInvestmentRequest {
    @Pattern(regexp = ".*\\S.*", message = "symbol must not be blank")
    @Size(max = 100)
    private String symbol;
    @Pattern(regexp = ".*\\S.*", message = "name must not be blank")
    @Size(max = 255)
    private String name;
    private InvestmentType type;
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "currency must be a 3-letter ISO code")
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
