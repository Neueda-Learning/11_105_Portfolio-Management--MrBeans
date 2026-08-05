package com.portfolio.portfolio_management.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TransactionRequestDTO {

    private String transactionType;
    private Double quantity;
    private Double pricePerUnit;
    private LocalDate transactionDate;
    private String notes;
}
