package com.portfoliomanager.dto.dividend;

import com.portfoliomanager.model.DividendMode;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public class SimulateDividendRequest {
    
    @NotNull
    private BigDecimal dividendPerShare;
    
    private BigDecimal reinvestmentPrice; // Required if mode == ACCUMULATIVE
    
    @NotNull
    private DividendMode mode;

    public BigDecimal getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(BigDecimal dividendPerShare) { this.dividendPerShare = dividendPerShare; }

    public BigDecimal getReinvestmentPrice() { return reinvestmentPrice; }
    public void setReinvestmentPrice(BigDecimal reinvestmentPrice) { this.reinvestmentPrice = reinvestmentPrice; }

    public DividendMode getMode() { return mode; }
    public void setMode(DividendMode mode) { this.mode = mode; }
}
