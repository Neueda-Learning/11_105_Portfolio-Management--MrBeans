package com.portfoliomanager.tool;

import com.portfoliomanager.dto.dashboard.DashboardSummaryResponse;
import com.portfoliomanager.service.DashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetPortfolioSummaryToolTest {

    @Mock private DashboardService dashboardService;

    private GetPortfolioSummaryTool tool;

    @BeforeEach
    void setUp() {
        tool = new GetPortfolioSummaryTool(dashboardService);
    }

    @Test
    void getName_ReturnsCorrectName() {
        assertEquals("get_portfolio_summary", tool.getName());
    }

    @Test
    void getDescription_ReturnsNonEmpty() {
        assertFalse(tool.getDescription().isEmpty());
    }

    @Test
    void getParameterSchema_ContainsType() {
        Map<String, Object> schema = tool.getParameterSchema();
        assertEquals("object", schema.get("type"));
    }

    @Test
    void execute_DelegatesAndMapsResult() {
        DashboardSummaryResponse summary = new DashboardSummaryResponse();
        summary.setTotalValue(new BigDecimal("50000.00"));
        summary.setTotalCostBasis(new BigDecimal("40000.00"));
        summary.setTotalUnrealisedPnl(new BigDecimal("10000.00"));
        summary.setTotalRealisedPnl(new BigDecimal("5000.00"));
        summary.setDividendIncomeThisYear(new BigDecimal("1200.00"));

        when(dashboardService.getSummary("USD")).thenReturn(summary);

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) tool.execute(Map.of("currency", "USD"));

        assertEquals(new BigDecimal("50000.00"), result.get("totalValue"));
        assertEquals(new BigDecimal("40000.00"), result.get("totalCostBasis"));
        assertEquals(new BigDecimal("10000.00"), result.get("unrealisedPnl"));
        assertEquals(new BigDecimal("5000.00"), result.get("realisedPnl"));
        assertEquals(new BigDecimal("1200.00"), result.get("dividendIncomeThisYear"));
        assertEquals("USD", result.get("currency"));
    }

    @Test
    void execute_DefaultCurrency() {
        DashboardSummaryResponse summary = new DashboardSummaryResponse();
        summary.setTotalValue(BigDecimal.ZERO);
        summary.setTotalCostBasis(BigDecimal.ZERO);
        summary.setTotalUnrealisedPnl(BigDecimal.ZERO);
        summary.setTotalRealisedPnl(BigDecimal.ZERO);
        summary.setDividendIncomeThisYear(BigDecimal.ZERO);

        when(dashboardService.getSummary("USD")).thenReturn(summary);

        tool.execute(Map.of());  // no currency → defaults to USD
        verify(dashboardService).getSummary("USD");
    }
}
