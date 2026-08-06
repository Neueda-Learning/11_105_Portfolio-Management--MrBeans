package com.portfoliomanager.controller;

import com.portfoliomanager.dto.dashboard.AllocationResponse;
import com.portfoliomanager.dto.dashboard.DashboardSummaryResponse;
import com.portfoliomanager.dto.dashboard.TrendResponse;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.service.DashboardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    private DashboardController dashboardController;

    @BeforeEach
    void setUp() {
        dashboardController = new DashboardController(dashboardService);
    }

    @Test
    void getSummary_DelegatesToService() {
        DashboardSummaryResponse expected = new DashboardSummaryResponse();
        expected.setTotalValue(new BigDecimal("123.45"));

        when(dashboardService.getSummary("USD")).thenReturn(expected);

        DashboardSummaryResponse response = dashboardController.getSummary("USD");

        assertEquals(new BigDecimal("123.45"), response.getTotalValue());
        verify(dashboardService).getSummary("USD");
    }

    @Test
    void getAllocation_DelegatesToService() {
        List<AllocationResponse> expected = List.of(
                new AllocationResponse(InvestmentType.STOCK, new BigDecimal("110.00"), new BigDecimal("55.00"))
        );

        when(dashboardService.getAllocation("INR")).thenReturn(expected);

        List<AllocationResponse> response = dashboardController.getAllocation("INR");

        assertEquals(1, response.size());
        assertEquals(InvestmentType.STOCK, response.get(0).getType());
        verify(dashboardService).getAllocation("INR");
    }

    @Test
    void getTrend_DelegatesToService() {
        List<TrendResponse> expected = List.of(
            new TrendResponse(LocalDate.now(), new BigDecimal("1000.00"), new BigDecimal("900.00"))
        );

        when(dashboardService.getTrend("INR", 30)).thenReturn(expected);

        List<TrendResponse> response = dashboardController.getTrend("INR", 30);

        assertEquals(1, response.size());
        verify(dashboardService).getTrend("INR", 30);
    }

    @Test
    void getTrendFiltered_DelegatesToService() {
        LocalDate fromDate = LocalDate.now().minusDays(7);
        LocalDate toDate = LocalDate.now();
        List<InvestmentType> types = List.of(InvestmentType.STOCK);
        List<TrendResponse> expected = List.of(
            new TrendResponse(fromDate, new BigDecimal("100.00"), new BigDecimal("90.00")),
            new TrendResponse(toDate, new BigDecimal("200.00"), new BigDecimal("180.00"))
        );

        when(dashboardService.getTrendFiltered("USD", fromDate, toDate, types, 30)).thenReturn(expected);

        List<TrendResponse> response = dashboardController.getTrendFiltered("USD", fromDate, toDate, types, 30);

        assertEquals(2, response.size());
        verify(dashboardService).getTrendFiltered("USD", fromDate, toDate, types, 30);
    }
}
