package com.portfoliomanager.dto.dashboard;

import java.math.BigDecimal;

public class PerformanceResponse {
    private String symbol;
    private String name;
    private BigDecimal returnPct;   // unrealised % return on cost basis
    private BigDecimal riskScore;   // volatility % (stddev of price changes) or type proxy
    private BigDecimal currentValue;
    private String investmentType;

    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public BigDecimal getReturnPct() { return returnPct; }
    public void setReturnPct(BigDecimal returnPct) { this.returnPct = returnPct; }

    public BigDecimal getRiskScore() { return riskScore; }
    public void setRiskScore(BigDecimal riskScore) { this.riskScore = riskScore; }

    public BigDecimal getCurrentValue() { return currentValue; }
    public void setCurrentValue(BigDecimal currentValue) { this.currentValue = currentValue; }

    public String getInvestmentType() { return investmentType; }
    public void setInvestmentType(String investmentType) { this.investmentType = investmentType; }
}
