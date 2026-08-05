package com.portfoliomanager.dtos;

import com.portfoliomanager.entity.InvestmentType;
import java.math.BigDecimal;

public class AllocationResponse {
    private InvestmentType type;
    private BigDecimal totalValue;
    private BigDecimal percentage;

    public AllocationResponse(InvestmentType type, BigDecimal totalValue, BigDecimal percentage) {
        this.type = type;
        this.totalValue = totalValue;
        this.percentage = percentage;
    }

    public InvestmentType getType() { return type; }
    public void setType(InvestmentType type) { this.type = type; }

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
