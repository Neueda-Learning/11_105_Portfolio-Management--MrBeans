package com.portfolio.portfolio_management.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class InvestmentResponseDTO {

    private Long investmentId;
    private String assetName;
    private String tickerSymbol;
    private String assetType;
    private Double quantity;
    private Double purchasePrice;
    private Double currentPrice;
    private LocalDate purchaseDate;
    private String currency;
    private Long portfolioId;
    private List<TransactionResponseDTO> transactions;
}
