package com.portfoliomanager.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TrendResponse {
    private LocalDate date;
    /** Total wealth = market value of remaining holdings + cumulative realised PnL */
    private BigDecimal portfolioValue;
    /** Cost basis of remaining holdings (amount still invested) */
    private BigDecimal investedAmount;

    public TrendResponse(LocalDate date, BigDecimal portfolioValue, BigDecimal investedAmount) {
        this.date = date;
        this.portfolioValue = portfolioValue;
        this.investedAmount = investedAmount;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getPortfolioValue() { return portfolioValue; }
    public void setPortfolioValue(BigDecimal portfolioValue) { this.portfolioValue = portfolioValue; }

    public BigDecimal getInvestedAmount() { return investedAmount; }
    public void setInvestedAmount(BigDecimal investedAmount) { this.investedAmount = investedAmount; }
}
