package com.portfoliomanager.controller;

import com.portfoliomanager.dto.dividend.CreateDividendRequest;
import com.portfoliomanager.dto.dividend.DividendResponse;
import com.portfoliomanager.dto.dividend.SimulateDividendRequest;
import com.portfoliomanager.dto.dividend.SimulateDividendResponse;
import com.portfoliomanager.service.DividendService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DividendControllerTest {

    @Mock private DividendService dividendService;

    private DividendController controller;

    @BeforeEach
    void setUp() {
        controller = new DividendController(dividendService);
    }

    @Test
    void getDividendsByInvestment_DelegatesToService() {
        UUID investmentId = UUID.randomUUID();
        DividendResponse resp = new DividendResponse();
        resp.setAmount(new BigDecimal("10.00"));
        when(dividendService.getDividendsByInvestment(investmentId))
                .thenReturn(List.of(resp));

        List<DividendResponse> result = controller.getDividendsByInvestment(investmentId);

        assertEquals(1, result.size());
        assertEquals(new BigDecimal("10.00"), result.get(0).getAmount());
    }

    @Test
    void createDividend_DelegatesToService() {
        UUID investmentId = UUID.randomUUID();
        CreateDividendRequest request = new CreateDividendRequest();
        DividendResponse resp = new DividendResponse();
        resp.setInvestmentId(investmentId);
        when(dividendService.createDividend(investmentId, request)).thenReturn(resp);

        DividendResponse result = controller.createDividend(investmentId, request);
        assertEquals(investmentId, result.getInvestmentId());
    }

    @Test
    void deleteDividend_DelegatesToService() {
        UUID investmentId = UUID.randomUUID();
        UUID dividendId = UUID.randomUUID();
        controller.deleteDividend(investmentId, dividendId);
        verify(dividendService).deleteDividend(investmentId, dividendId);
    }

    @Test
    void simulateDividend_DelegatesToService() {
        UUID investmentId = UUID.randomUUID();
        SimulateDividendRequest request = new SimulateDividendRequest();
        SimulateDividendResponse resp = new SimulateDividendResponse();
        resp.setTotalDividendAmount(new BigDecimal("8.50"));
        when(dividendService.simulateDividend(investmentId, request)).thenReturn(resp);

        SimulateDividendResponse result = controller.simulateDividend(investmentId, request);
        assertEquals(new BigDecimal("8.50"), result.getTotalDividendAmount());
    }
}
