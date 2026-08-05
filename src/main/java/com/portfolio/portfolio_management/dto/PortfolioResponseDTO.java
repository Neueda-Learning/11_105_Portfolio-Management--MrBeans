package com.portfolio.portfolio_management.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PortfolioResponseDTO {

    private Long portfolioId;
    private String portfolioName;
    private List<InvestmentResponseDTO> investments;
}
