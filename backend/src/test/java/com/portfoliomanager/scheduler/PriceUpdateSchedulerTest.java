package com.portfoliomanager.scheduler;

import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.service.PriceSnapshotService;
import com.portfoliomanager.client.FakePriceFeedClient;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.client.PriceFeedClient;
import com.portfoliomanager.model.InvestmentType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceUpdateSchedulerTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private PriceSnapshotService priceSnapshotService;

    private FakePriceFeedClient fakePriceFeedClient;

    private PriceUpdateScheduler priceUpdateScheduler;

    @BeforeEach
    void setUp() {
        // Use the FakePriceFeedClient explicitly per Section 7 (no live network calls in tests)
        fakePriceFeedClient = new FakePriceFeedClient();
        priceUpdateScheduler = new PriceUpdateScheduler(investmentRepository, fakePriceFeedClient, priceSnapshotService);
    }

    @Test
    void updatePrices_SucceedsForValidTickers() {
        Investment inv1 = createInvestment(UUID.randomUUID(), "AAPL");
        Investment inv2 = createInvestment(UUID.randomUUID(), "MSFT");
        
        when(investmentRepository.findAll()).thenReturn(List.of(inv1, inv2));

        // fakePriceFeedClient already has AAPL and MSFT hardcoded by default
        
        priceUpdateScheduler.updatePrices();

        // Verify service was called to save the snapshots
        verify(priceSnapshotService, times(1)).saveSnapshot(eq(inv1.getId()), eq(new BigDecimal("150.0000")), eq("USD"), any());
        verify(priceSnapshotService, times(1)).saveSnapshot(eq(inv2.getId()), eq(new BigDecimal("300.0000")), eq("USD"), any());
    }

    @Test
    void updatePrices_SkipsInvalidTickerGracefully() {
        Investment validInv = createInvestment(UUID.randomUUID(), "AAPL");
        Investment invalidInv = createInvestment(UUID.randomUUID(), "INVALID_TICKER");
        
        when(investmentRepository.findAll()).thenReturn(List.of(validInv, invalidInv));

        priceUpdateScheduler.updatePrices();

        // Should save AAPL
        verify(priceSnapshotService, times(1)).saveSnapshot(eq(validInv.getId()), eq(new BigDecimal("150.0000")), eq("USD"), any());
        // Should NOT save the invalid ticker, but the job shouldn't crash
        verify(priceSnapshotService, never()).saveSnapshot(eq(invalidInv.getId()), any(), any(), any());
    }

    @Test
    void updatePrices_SkipsCycleIfClientThrowsException() {
        // Create a mock that throws an exception simulating a network partition
        PriceFeedClient failingClient = mock(PriceFeedClient.class);
        when(failingClient.getPrices(anyList())).thenThrow(new RuntimeException("Simulated network error"));
        
        PriceUpdateScheduler failingScheduler = new PriceUpdateScheduler(investmentRepository, failingClient, priceSnapshotService);
        
        Investment inv1 = createInvestment(UUID.randomUUID(), "AAPL");
        when(investmentRepository.findAll()).thenReturn(List.of(inv1));

        // Calling updatePrices should catch the exception and return gracefully without throwing it up
        failingScheduler.updatePrices();

        // Should not have attempted to save anything
        verify(priceSnapshotService, never()).saveSnapshot(any(), any(), any(), any());
    }
    
    private Investment createInvestment(UUID id, String symbol) {
        Investment investment = new Investment();
        investment.setId(id);
        investment.setSymbol(symbol);
        investment.setType(InvestmentType.STOCK);
        investment.setCurrency("USD");
        return investment;
    }
}
