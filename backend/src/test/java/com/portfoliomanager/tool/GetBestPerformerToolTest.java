package com.portfoliomanager.tool;

import com.portfoliomanager.dto.dashboard.PerformanceResponse;
import com.portfoliomanager.service.DashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetBestPerformerToolTest {

    @Mock private DashboardService dashboardService;

    private GetBestPerformerTool tool;

    @BeforeEach
    void setUp() {
        tool = new GetBestPerformerTool(dashboardService);
    }

    @Test
    void getName_ReturnsCorrectName() {
        assertEquals("get_best_performer", tool.getName());
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
    void execute_ReturnsTopPerformer() {
        PerformanceResponse p1 = new PerformanceResponse();
        p1.setSymbol("AAPL");
        p1.setName("Apple Inc.");
        p1.setReturnPct(new BigDecimal("25.00"));
        p1.setCurrentValue(new BigDecimal("5000.00"));
        p1.setInvestmentType("STOCK");

        PerformanceResponse p2 = new PerformanceResponse();
        p2.setSymbol("MSFT");
        p2.setName("Microsoft");
        p2.setReturnPct(new BigDecimal("15.00"));
        p2.setCurrentValue(new BigDecimal("3000.00"));
        p2.setInvestmentType("STOCK");

        when(dashboardService.getPerformance("USD")).thenReturn(List.of(p1, p2));

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) tool.execute(Map.of("currency", "USD"));

        assertEquals("AAPL", result.get("symbol"));
        assertEquals("Apple Inc.", result.get("name"));
        assertEquals(new BigDecimal("25.00"), result.get("returnPct"));
    }

    @Test
    void execute_EmptyList_ReturnsNoDataMessage() {
        when(dashboardService.getPerformance("USD")).thenReturn(List.of());

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) tool.execute(Map.of("currency", "USD"));

        assertEquals("No investments with price data found", result.get("message"));
    }

    @Test
    void execute_NullReturnPct_FilteredOut() {
        PerformanceResponse p1 = new PerformanceResponse();
        p1.setSymbol("BOND1");
        p1.setReturnPct(null); // should be filtered out

        PerformanceResponse p2 = new PerformanceResponse();
        p2.setSymbol("AAPL");
        p2.setName("Apple");
        p2.setReturnPct(new BigDecimal("10.00"));
        p2.setCurrentValue(new BigDecimal("1000.00"));
        p2.setInvestmentType("STOCK");

        when(dashboardService.getPerformance("USD")).thenReturn(List.of(p1, p2));

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) tool.execute(Map.of("currency", "USD"));

        assertEquals("AAPL", result.get("symbol"));
    }

    @Test
    void execute_NullName_FallsBackToSymbol() {
        PerformanceResponse p1 = new PerformanceResponse();
        p1.setSymbol("XYZ");
        p1.setName(null);
        p1.setReturnPct(new BigDecimal("5.00"));
        p1.setCurrentValue(new BigDecimal("500.00"));
        p1.setInvestmentType("STOCK");

        when(dashboardService.getPerformance("USD")).thenReturn(List.of(p1));

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) tool.execute(Map.of("currency", "USD"));

        assertEquals("XYZ", result.get("name")); // fallback to symbol
    }

    @Test
    void execute_NullInvestmentType_FallsBackToUnknown() {
        PerformanceResponse p1 = new PerformanceResponse();
        p1.setSymbol("XYZ");
        p1.setName("Test");
        p1.setReturnPct(new BigDecimal("5.00"));
        p1.setCurrentValue(new BigDecimal("500.00"));
        p1.setInvestmentType(null);

        when(dashboardService.getPerformance("USD")).thenReturn(List.of(p1));

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) tool.execute(Map.of("currency", "USD"));

        assertEquals("UNKNOWN", result.get("investmentType"));
    }
}
