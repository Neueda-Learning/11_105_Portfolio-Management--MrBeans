package com.portfoliomanager.service;

import com.portfoliomanager.repository.DividendRepository;
import com.portfoliomanager.repository.TransactionRepository;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;
import com.portfoliomanager.dto.dividend.SimulateDividendRequest;
import com.portfoliomanager.model.DividendMode;
import com.portfoliomanager.dto.dividend.SimulateDividendResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DividendServiceTest {

    @Mock
    private DividendRepository dividendRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private DividendService dividendService;

    private UUID investmentId;
    private List<Transaction> mockTransactions;

    @BeforeEach
    void setUp() {
        investmentId = UUID.randomUUID();
        
        // Setup holdings: Buy 100 shares total
        Transaction t1 = new Transaction();
        t1.setType(TransactionType.BUY);
        t1.setQuantity(new BigDecimal("100.0"));
        t1.setPrice(new BigDecimal("50.0"));
        t1.setFxRateToHome(BigDecimal.ONE);
        
        mockTransactions = List.of(t1);
    }

    @Test
    void simulateDividend_DistributiveMode_CalculatesCashPayout() {
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId))
                .thenReturn(mockTransactions);

        SimulateDividendRequest request = new SimulateDividendRequest();
        request.setMode(DividendMode.DISTRIBUTIVE);
        request.setDividendPerShare(new BigDecimal("1.50")); // $1.50 per share

        SimulateDividendResponse response = dividendService.simulateDividend(investmentId, request);

        // 100 shares * 1.50 = 150
        assertEquals(0, new BigDecimal("100.00000000").compareTo(response.getTotalShares()));
        assertEquals(0, new BigDecimal("150.00").compareTo(response.getTotalDividendAmount()));
        assertEquals(0, new BigDecimal("150.00").compareTo(response.getCashPayout()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getNewSharesAcquired()));
    }

    @Test
    void simulateDividend_AccumulativeMode_CalculatesNewShares() {
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId))
                .thenReturn(mockTransactions);

        SimulateDividendRequest request = new SimulateDividendRequest();
        request.setMode(DividendMode.ACCUMULATIVE);
        request.setDividendPerShare(new BigDecimal("2.00")); // $2.00 per share
        request.setReinvestmentPrice(new BigDecimal("40.00")); // Buying back at $40/share

        SimulateDividendResponse response = dividendService.simulateDividend(investmentId, request);

        // 100 shares * 2.00 = $200 total payout. $200 / $40 = 5 new shares.
        assertEquals(0, new BigDecimal("200.00").compareTo(response.getTotalDividendAmount()));
        assertEquals(0, new BigDecimal("5.00000000").compareTo(response.getNewSharesAcquired()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.getCashPayout()));
    }

    @Test
    void simulateDividend_AccumulativeMode_ThrowsIfNoReinvestmentPrice() {
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId))
                .thenReturn(mockTransactions);

        SimulateDividendRequest request = new SimulateDividendRequest();
        request.setMode(DividendMode.ACCUMULATIVE);
        request.setDividendPerShare(new BigDecimal("2.00"));
        request.setReinvestmentPrice(BigDecimal.ZERO); // Invalid

        assertThrows(IllegalArgumentException.class, () -> 
            dividendService.simulateDividend(investmentId, request)
        );
    }
}
