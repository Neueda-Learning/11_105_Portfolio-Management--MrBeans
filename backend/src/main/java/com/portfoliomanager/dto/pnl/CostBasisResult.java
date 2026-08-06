package com.portfoliomanager.dto.pnl;

import java.math.BigDecimal;

public record CostBasisResult(
    BigDecimal totalQuantity,
    BigDecimal avgCostLocal,
    BigDecimal avgCostHome,
    BigDecimal realisedPnlLocal,
    BigDecimal realisedPnlHome
) {}
