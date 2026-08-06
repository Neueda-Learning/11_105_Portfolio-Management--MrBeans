package com.portfoliomanager.dto.dividend;

import com.portfoliomanager.model.DividendMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateDividendRequest {

    @NotNull
    @Positive
    private BigDecimal amount;

    @Positive
    private BigDecimal dividendPerShare; // Optional: stored for reference

    @NotBlank
    @Pattern(regexp = "^[A-Za-z]{3}$", message = "currency must be a 3-letter ISO code")
    private String currency;

    @PositiveOrZero
    private BigDecimal withholdingTax = BigDecimal.ZERO; // Tax deducted at source

    @Positive
    private BigDecimal reinvestmentPrice; // Required for ACCUMULATIVE auto-BUY

    @NotNull
    private DividendMode mode;

    private LocalDate exDate;

    @NotNull
    private LocalDate paymentDate;

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
}
