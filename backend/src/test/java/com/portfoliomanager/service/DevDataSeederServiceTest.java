package com.portfoliomanager.service;

import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.PriceSnapshot;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.repository.DividendRepository;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.PriceSnapshotRepository;
import com.portfoliomanager.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DevDataSeederServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PriceSnapshotRepository priceSnapshotRepository;

    @Mock
    private DividendRepository dividendRepository;

    @InjectMocks
    private DevDataSeederService seederService;

    @Test
    void seed_WipeEnabled_CreatesExpectedCountsAndData() {
        when(investmentRepository.save(any(Investment.class))).thenAnswer(invocation -> {
            Investment investment = invocation.getArgument(0);
            if (investment.getId() == null) {
                investment.setId(UUID.randomUUID());
            }
            return investment;
        });
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(priceSnapshotRepository.save(any(PriceSnapshot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DevDataSeederService.SeedSummary summary = seederService.seed(2, 3, 4, true);

        assertEquals(2, summary.investments());
        assertEquals(6, summary.transactions());
        assertEquals(8, summary.priceSnapshots());

        verify(dividendRepository).deleteAllInBatch();
        verify(priceSnapshotRepository).deleteAllInBatch();
        verify(transactionRepository).deleteAllInBatch();
        verify(investmentRepository).deleteAllInBatch();

        verify(investmentRepository, times(2)).save(any(Investment.class));
        verify(transactionRepository, times(6)).save(any(Transaction.class));
        verify(priceSnapshotRepository, times(8)).save(any(PriceSnapshot.class));

        ArgumentCaptor<Investment> investmentCaptor = ArgumentCaptor.forClass(Investment.class);
        verify(investmentRepository, times(2)).save(investmentCaptor.capture());
        for (Investment investment : investmentCaptor.getAllValues()) {
            assertTrue(investment.getSymbol().startsWith("SIM"));
            assertEquals("USD", investment.getCurrency());
            assertNotNull(investment.getMetadata());
            assertEquals(Boolean.TRUE, investment.getMetadata().get("seeded"));
        }

        ArgumentCaptor<Transaction> transactionCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(6)).save(transactionCaptor.capture());
        for (Transaction transaction : transactionCaptor.getAllValues()) {
            assertNotNull(transaction.getInvestmentId());
            assertNotNull(transaction.getType());
            assertNotNull(transaction.getTxnDate());
            assertNotNull(transaction.getQuantity());
            assertTrue(transaction.getQuantity().compareTo(BigDecimal.ZERO) > 0);
            assertNotNull(transaction.getPrice());
            assertTrue(transaction.getPrice().compareTo(BigDecimal.ONE) >= 0);
            assertEquals("USD", transaction.getCurrency());
            assertEquals(BigDecimal.ONE, transaction.getFxRateToHome());
        }

        ArgumentCaptor<PriceSnapshot> snapshotCaptor = ArgumentCaptor.forClass(PriceSnapshot.class);
        verify(priceSnapshotRepository, times(8)).save(snapshotCaptor.capture());
        for (PriceSnapshot snapshot : snapshotCaptor.getAllValues()) {
            assertNotNull(snapshot.getInvestmentId());
            assertNotNull(snapshot.getFetchedAt());
            assertEquals("USD", snapshot.getCurrency());
            assertNotNull(snapshot.getPrice());
            assertTrue(snapshot.getPrice().compareTo(BigDecimal.ONE) >= 0);
        }
    }

    @Test
    void seed_WipeDisabled_SkipsDeletesAndSupportsZeroGeneratedRows() {
        when(investmentRepository.save(any(Investment.class))).thenAnswer(invocation -> {
            Investment investment = invocation.getArgument(0);
            if (investment.getId() == null) {
                investment.setId(UUID.randomUUID());
            }
            return investment;
        });

        DevDataSeederService.SeedSummary summary = seederService.seed(1, 0, 0, false);

        assertEquals(1, summary.investments());
        assertEquals(0, summary.transactions());
        assertEquals(0, summary.priceSnapshots());

        verify(dividendRepository, never()).deleteAllInBatch();
        verify(priceSnapshotRepository, never()).deleteAllInBatch();
        verify(transactionRepository, never()).deleteAllInBatch();
        verify(investmentRepository, never()).deleteAllInBatch();

        verify(investmentRepository, times(1)).save(any(Investment.class));
        verify(transactionRepository, never()).save(any(Transaction.class));
        verify(priceSnapshotRepository, never()).save(any(PriceSnapshot.class));
    }
}
