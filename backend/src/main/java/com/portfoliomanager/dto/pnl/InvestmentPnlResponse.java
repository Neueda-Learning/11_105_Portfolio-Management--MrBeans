package com.portfoliomanager.dto.pnl;

import java.math.BigDecimal;

public class InvestmentPnlResponse {
    private BigDecimal realisedPnl;
    private BigDecimal unrealisedPnl;
    private BigDecimal realisedPnlLocal;
    private BigDecimal unrealisedPnlLocal;
    private BigDecimal totalCostBasis;
    private BigDecimal currentQuantity;

    public BigDecimal getRealisedPnl() { return realisedPnl; }
    public void setRealisedPnl(BigDecimal realisedPnl) { this.realisedPnl = realisedPnl; }

    public BigDecimal getUnrealisedPnl() { return unrealisedPnl; }
    public void setUnrealisedPnl(BigDecimal unrealisedPnl) { this.unrealisedPnl = unrealisedPnl; }

    public BigDecimal getRealisedPnlLocal() { return realisedPnlLocal; }
    public void setRealisedPnlLocal(BigDecimal realisedPnlLocal) { this.realisedPnlLocal = realisedPnlLocal; }

    public BigDecimal getUnrealisedPnlLocal() { return unrealisedPnlLocal; }
    public void setUnrealisedPnlLocal(BigDecimal unrealisedPnlLocal) { this.unrealisedPnlLocal = unrealisedPnlLocal; }

    public BigDecimal getTotalCostBasis() { return totalCostBasis; }
    public void setTotalCostBasis(BigDecimal totalCostBasis) { this.totalCostBasis = totalCostBasis; }

    public BigDecimal getCurrentQuantity() { return currentQuantity; }
    public void setCurrentQuantity(BigDecimal currentQuantity) { this.currentQuantity = currentQuantity; }
}
