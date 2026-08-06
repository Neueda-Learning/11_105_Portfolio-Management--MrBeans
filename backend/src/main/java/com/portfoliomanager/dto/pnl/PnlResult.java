package com.portfoliomanager.dto.pnl;

import java.math.BigDecimal;

public record PnlResult(
    BigDecimal realisedPnl,       // Home currency
    BigDecimal unrealisedPnl,     // Home currency
    BigDecimal realisedPnlLocal,  // Local currency
    BigDecimal unrealisedPnlLocal,// Local currency
    BigDecimal totalCostBasis,    // Home currency
    BigDecimal currentQuantity
) {}
