package com.portfoliomanager.dtos;

import java.math.BigDecimal;

public record CostBasisResult(
    BigDecimal totalQuantity,
    BigDecimal avgCostLocal,
    BigDecimal avgCostHome,
    BigDecimal realisedPnlLocal,
    BigDecimal realisedPnlHome
) {}
