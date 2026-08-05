package com.portfolio.portfolio_management.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class TransactionResponseDTO {

    private Long transactionId;
    private String transactionType;
    private Double quantity;
    private Double pricePerUnit;
    private LocalDate transactionDate;
    private String notes;
    private Long investmentId;
}
