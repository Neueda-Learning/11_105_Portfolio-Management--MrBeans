package com.portfoliomanager.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TrendResponse {
    private LocalDate date;
    private BigDecimal portfolioValue;

    public TrendResponse(LocalDate date, BigDecimal portfolioValue) {
        this.date = date;
        this.portfolioValue = portfolioValue;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public BigDecimal getPortfolioValue() { return portfolioValue; }
    public void setPortfolioValue(BigDecimal portfolioValue) { this.portfolioValue = portfolioValue; }
}
