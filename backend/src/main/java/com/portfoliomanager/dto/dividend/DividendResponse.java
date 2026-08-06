package com.portfoliomanager.dto.dividend;

import com.portfoliomanager.model.DividendMode;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class DividendResponse {

    private UUID id;
    private UUID investmentId;
    private BigDecimal amount;
    private BigDecimal dividendPerShare;
    private String currency;
    private BigDecimal withholdingTax;
    private BigDecimal reinvestmentPrice;
    private DividendMode mode;
    private LocalDate exDate;
    private LocalDate paymentDate;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getInvestmentId() { return investmentId; }
    public void setInvestmentId(UUID investmentId) { this.investmentId = investmentId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public BigDecimal getDividendPerShare() { return dividendPerShare; }
    public void setDividendPerShare(BigDecimal dividendPerShare) { this.dividendPerShare = dividendPerShare; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getWithholdingTax() { return withholdingTax; }
    public void setWithholdingTax(BigDecimal withholdingTax) { this.withholdingTax = withholdingTax; }

    public BigDecimal getReinvestmentPrice() { return reinvestmentPrice; }
    public void setReinvestmentPrice(BigDecimal reinvestmentPrice) { this.reinvestmentPrice = reinvestmentPrice; }

    public DividendMode getMode() { return mode; }
    public void setMode(DividendMode mode) { this.mode = mode; }

    public LocalDate getExDate() { return exDate; }
    public void setExDate(LocalDate exDate) { this.exDate = exDate; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
