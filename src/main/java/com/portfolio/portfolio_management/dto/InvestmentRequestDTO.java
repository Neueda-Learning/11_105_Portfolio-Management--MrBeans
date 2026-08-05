package com.portfolio.portfolio_management.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class InvestmentRequestDTO {

    private String assetName;
    private String tickerSymbol;
    private String assetType;
    private Double quantity;
    private Double purchasePrice;
    private Double currentPrice;
    private LocalDate purchaseDate;
    private String currency;
}
