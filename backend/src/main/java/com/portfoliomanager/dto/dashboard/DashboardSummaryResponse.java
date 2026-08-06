package com.portfoliomanager.dto.dashboard;

import java.math.BigDecimal;

public class DashboardSummaryResponse {
    private BigDecimal totalValue;
    private BigDecimal totalCostBasis;
    private BigDecimal totalRealisedPnl;
    private BigDecimal totalUnrealisedPnl;

    public BigDecimal getTotalValue() { return totalValue; }
    public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }

    public BigDecimal getTotalCostBasis() { return totalCostBasis; }
    public void setTotalCostBasis(BigDecimal totalCostBasis) { this.totalCostBasis = totalCostBasis; }

    public BigDecimal getTotalRealisedPnl() { return totalRealisedPnl; }
    public void setTotalRealisedPnl(BigDecimal totalRealisedPnl) { this.totalRealisedPnl = totalRealisedPnl; }

    public BigDecimal getTotalUnrealisedPnl() { return totalUnrealisedPnl; }
    public void setTotalUnrealisedPnl(BigDecimal totalUnrealisedPnl) { this.totalUnrealisedPnl = totalUnrealisedPnl; }
}
