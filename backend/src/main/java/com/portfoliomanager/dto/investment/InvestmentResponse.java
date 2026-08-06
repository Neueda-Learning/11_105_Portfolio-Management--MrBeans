package com.portfoliomanager.dto.investment;

import com.portfoliomanager.model.InvestmentType;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class InvestmentResponse {
    private UUID id;
    private String symbol;
    private String name;
    private InvestmentType type;
    private String currency;
    private Map<String, Object> metadata;
    private Instant createdAt;
    private Instant updatedAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

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

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
