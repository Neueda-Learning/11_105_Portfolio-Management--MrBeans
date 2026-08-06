package com.portfoliomanager.dto.transaction;

import com.portfoliomanager.model.TransactionType;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class CreateTransactionRequest {
    
    @NotNull
    private TransactionType type;
    
    private BigDecimal quantity;
    private BigDecimal price;
    private String currency;
    private BigDecimal fxRateToHome;
    
    @NotNull
    private LocalDate txnDate;

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
}
