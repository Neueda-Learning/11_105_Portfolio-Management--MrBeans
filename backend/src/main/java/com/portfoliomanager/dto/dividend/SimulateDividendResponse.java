package com.portfoliomanager.dto.dividend;

import java.math.BigDecimal;

public class SimulateDividendResponse {
    private BigDecimal totalShares;
    private BigDecimal totalDividendAmount;
    private BigDecimal newSharesAcquired;
    private BigDecimal cashPayout;

    public BigDecimal getTotalShares() { return totalShares; }
    public void setTotalShares(BigDecimal totalShares) { this.totalShares = totalShares; }

    public BigDecimal getTotalDividendAmount() { return totalDividendAmount; }
    public void setTotalDividendAmount(BigDecimal totalDividendAmount) { this.totalDividendAmount = totalDividendAmount; }

    public BigDecimal getNewSharesAcquired() { return newSharesAcquired; }
    public void setNewSharesAcquired(BigDecimal newSharesAcquired) { this.newSharesAcquired = newSharesAcquired; }

    public BigDecimal getCashPayout() { return cashPayout; }
    public void setCashPayout(BigDecimal cashPayout) { this.cashPayout = cashPayout; }
}
