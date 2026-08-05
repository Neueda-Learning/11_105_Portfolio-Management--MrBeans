package com.portfoliomanager.dtos;

import com.portfoliomanager.entity.TransactionType;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class TransactionResponse {
    private UUID id;
    private UUID investmentId;
    private TransactionType type;
    private BigDecimal quantity;
    private BigDecimal price;
    private String currency;
    private BigDecimal fxRateToHome;
    private LocalDate txnDate;
    private Instant createdAt;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getInvestmentId() { return investmentId; }
    public void setInvestmentId(UUID investmentId) { this.investmentId = investmentId; }

    public TransactionType getType() { return type; }
    public void setType(TransactionType type) { this.type = type; }

    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public BigDecimal getFxRateToHome() { return fxRateToHome; }
    public void setFxRateToHome(BigDecimal fxRateToHome) { this.fxRateToHome = fxRateToHome; }

    public LocalDate getTxnDate() { return txnDate; }
    public void setTxnDate(LocalDate txnDate) { this.txnDate = txnDate; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
