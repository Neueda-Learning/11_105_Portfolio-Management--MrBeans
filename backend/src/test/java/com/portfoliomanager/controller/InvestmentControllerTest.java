package com.portfoliomanager.controller;

import com.portfoliomanager.dto.investment.CreateInvestmentRequest;
import com.portfoliomanager.dto.investment.InvestmentResponse;
import com.portfoliomanager.dto.investment.UpdateInvestmentRequest;
import com.portfoliomanager.dto.pnl.InvestmentPnlResponse;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.service.DashboardService;
import com.portfoliomanager.service.InvestmentService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InvestmentControllerTest {

    @Mock private InvestmentService investmentService;
    @Mock private DashboardService dashboardService;

    private InvestmentController controller;

    @BeforeEach
    void setUp() {
        controller = new InvestmentController(investmentService, dashboardService);
    }

    @Test
    void getAllInvestments_DelegatesToService() {
        InvestmentResponse resp = new InvestmentResponse();
        resp.setSymbol("AAPL");
        when(investmentService.getAllInvestments()).thenReturn(List.of(resp));

        List<InvestmentResponse> result = controller.getAllInvestments();

        assertEquals(1, result.size());
        assertEquals("AAPL", result.get(0).getSymbol());
    }

    @Test
    void getInvestmentById_DelegatesToService() {
        UUID id = UUID.randomUUID();
        InvestmentResponse resp = new InvestmentResponse();
        resp.setId(id);
        when(investmentService.getInvestmentById(id)).thenReturn(resp);

        InvestmentResponse result = controller.getInvestmentById(id);
        assertEquals(id, result.getId());
    }

    @Test
    void createInvestment_DelegatesToService() {
        CreateInvestmentRequest request = new CreateInvestmentRequest();
        request.setSymbol("MSFT");
        InvestmentResponse resp = new InvestmentResponse();
        resp.setSymbol("MSFT");
        when(investmentService.createInvestment(request)).thenReturn(resp);

        InvestmentResponse result = controller.createInvestment(request);
        assertEquals("MSFT", result.getSymbol());
    }

    @Test
    void updateInvestment_DelegatesToService() {
        UUID id = UUID.randomUUID();
        UpdateInvestmentRequest request = new UpdateInvestmentRequest();
        InvestmentResponse resp = new InvestmentResponse();
        resp.setId(id);
        when(investmentService.updateInvestment(id, request)).thenReturn(resp);

        InvestmentResponse result = controller.updateInvestment(id, request);
        assertEquals(id, result.getId());
    }

    @Test
    void deleteInvestment_DelegatesToService() {
        UUID id = UUID.randomUUID();
        controller.deleteInvestment(id);
        verify(investmentService).deleteInvestment(id);
    }

    @Test
    void getInvestmentPnl_DelegatesToDashboardService() {
        UUID id = UUID.randomUUID();
        InvestmentPnlResponse resp = new InvestmentPnlResponse();
        resp.setUnrealisedPnl(new BigDecimal("100.00"));
        when(dashboardService.getInvestmentPnl(id, "INR")).thenReturn(resp);

        InvestmentPnlResponse result = controller.getInvestmentPnl(id, "INR");
        assertEquals(new BigDecimal("100.00"), result.getUnrealisedPnl());
    }

    @Test
    void getCurrentPrice_DelegatesToService() {
        UUID id = UUID.randomUUID();
        Map<String, Object> expected = Map.of("price", 150.0, "currency", "USD");
        when(investmentService.getCurrentPrice(id)).thenReturn(expected);

        Map<String, Object> result = controller.getCurrentPrice(id);
        assertEquals(150.0, result.get("price"));
    }
}
