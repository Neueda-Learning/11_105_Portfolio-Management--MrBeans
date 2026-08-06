package com.portfoliomanager.service;

import com.portfoliomanager.dto.dividend.CreateDividendRequest;
import com.portfoliomanager.repository.DividendRepository;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.TransactionRepository;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.dto.dividend.DividendResponse;
import com.portfoliomanager.model.Dividend;
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
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DividendServiceTest {

    @Mock
    private DividendRepository dividendRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private DividendService dividendService;

    private UUID investmentId;
    private UUID dividendId;
    private List<Transaction> mockTransactions;

    @BeforeEach
    void setUp() {
        investmentId = UUID.randomUUID();
        dividendId = UUID.randomUUID();
        
        // Setup holdings: Buy 100 shares total
        Transaction t1 = new Transaction();
        t1.setType(TransactionType.BUY);
        t1.setQuantity(new BigDecimal("100.0"));
        t1.setPrice(new BigDecimal("50.0"));
        t1.setFxRateToHome(BigDecimal.ONE);
        
        mockTransactions = List.of(t1);
    }

    @Test
    void getDividendsByInvestment_NotFound_Throws() {
        when(investmentRepository.existsById(investmentId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> dividendService.getDividendsByInvestment(investmentId));
    }

    @Test
    void getDividendsByInvestment_Success_MapsResponse() {
        Dividend dividend = new Dividend();
        dividend.setId(dividendId);
        dividend.setInvestmentId(investmentId);
        dividend.setAmount(new BigDecimal("100.0000"));
        dividend.setDividendPerShare(new BigDecimal("1.25000000"));
        dividend.setCurrency("USD");
        dividend.setWithholdingTax(new BigDecimal("10.0000"));
        dividend.setReinvestmentPrice(new BigDecimal("40.0000"));
        dividend.setMode(DividendMode.ACCUMULATIVE);
        dividend.setExDate(LocalDate.of(2026, 8, 1));
        dividend.setPaymentDate(LocalDate.of(2026, 8, 5));
        dividend.setCreatedAt(Instant.now());

        when(investmentRepository.existsById(investmentId)).thenReturn(true);
        when(dividendRepository.findByInvestmentIdOrderByPaymentDateDesc(investmentId)).thenReturn(List.of(dividend));

        List<DividendResponse> responses = dividendService.getDividendsByInvestment(investmentId);

        assertEquals(1, responses.size());
        assertEquals(dividendId, responses.get(0).getId());
        assertEquals("USD", responses.get(0).getCurrency());
    }

    @Test
    void createDividend_NotFound_Throws() {
        when(investmentRepository.existsById(investmentId)).thenReturn(false);

        CreateDividendRequest request = new CreateDividendRequest();
        request.setAmount(new BigDecimal("100.00"));
        request.setCurrency("USD");
        request.setMode(DividendMode.DISTRIBUTIVE);
        request.setPaymentDate(LocalDate.now());

        assertThrows(ResourceNotFoundException.class, () -> dividendService.createDividend(investmentId, request));
    }

    @Test
    void createDividend_Distributive_SavesDividendOnly() {
        when(investmentRepository.existsById(investmentId)).thenReturn(true);
        when(dividendRepository.save(any(Dividend.class))).thenAnswer(invocation -> {
            Dividend saved = invocation.getArgument(0);
            saved.setId(dividendId);
            return saved;
        });

        CreateDividendRequest request = new CreateDividendRequest();
        request.setAmount(new BigDecimal("120.5678"));
        request.setCurrency(" usd ");
        request.setMode(DividendMode.DISTRIBUTIVE);
        request.setPaymentDate(LocalDate.of(2026, 8, 10));

        DividendResponse response = dividendService.createDividend(investmentId, request);

        assertNotNull(response.getId());
        assertEquals("USD", response.getCurrency());
        assertEquals(0, new BigDecimal("120.57").compareTo(response.getAmount()));
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void createDividend_Accumulative_WithReinvestment_CreatesBuyTransaction() {
        when(investmentRepository.existsById(investmentId)).thenReturn(true);
        when(dividendRepository.save(any(Dividend.class))).thenAnswer(invocation -> {
            Dividend saved = invocation.getArgument(0);
            saved.setId(dividendId);
            return saved;
        });

        CreateDividendRequest request = new CreateDividendRequest();
        request.setAmount(new BigDecimal("200.00"));
        request.setWithholdingTax(new BigDecimal("20.00"));
        request.setCurrency("usd");
        request.setMode(DividendMode.ACCUMULATIVE);
        request.setReinvestmentPrice(new BigDecimal("45.00"));
        request.setPaymentDate(LocalDate.of(2026, 8, 10));

        dividendService.createDividend(investmentId, request);

        ArgumentCaptor<Transaction> txCaptor = ArgumentCaptor.forClass(Transaction.class);
        verify(transactionRepository, times(1)).save(txCaptor.capture());
        Transaction tx = txCaptor.getValue();
        assertEquals(TransactionType.BUY, tx.getType());
        assertEquals(investmentId, tx.getInvestmentId());
        assertEquals("USD", tx.getCurrency());
    }

    @Test
    void deleteDividend_NotFound_Throws() {
        when(dividendRepository.findById(dividendId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> dividendService.deleteDividend(investmentId, dividendId));
    }

    @Test
    void deleteDividend_WrongInvestment_Throws() {
        Dividend dividend = new Dividend();
        dividend.setId(dividendId);
        dividend.setInvestmentId(UUID.randomUUID());
        when(dividendRepository.findById(dividendId)).thenReturn(Optional.of(dividend));

        assertThrows(ResourceNotFoundException.class, () -> dividendService.deleteDividend(investmentId, dividendId));
    }

    @Test
    void deleteDividend_Success() {
        Dividend dividend = new Dividend();
        dividend.setId(dividendId);
        dividend.setInvestmentId(investmentId);
        when(dividendRepository.findById(dividendId)).thenReturn(Optional.of(dividend));

        dividendService.deleteDividend(investmentId, dividendId);

        verify(dividendRepository, times(1)).delete(dividend);
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
