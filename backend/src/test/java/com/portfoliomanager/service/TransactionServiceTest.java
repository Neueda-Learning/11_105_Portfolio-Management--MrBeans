package com.portfoliomanager.service;

import com.portfoliomanager.dto.transaction.CreateTransactionRequest;
import com.portfoliomanager.dto.transaction.TransactionResponse;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;
import com.portfoliomanager.repository.TransactionRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private FxRateService fxRateService;

    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionService(transactionRepository, fxRateService);
    }

    @Test
    void getTransactionsByInvestmentId_ReturnsMappedList() {
        UUID investmentId = UUID.randomUUID();
        Transaction txn = buildTransaction(investmentId, TransactionType.BUY, "10", "100.00");

        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId))
                .thenReturn(List.of(txn));

        List<TransactionResponse> result = transactionService.getTransactionsByInvestmentId(investmentId);

        assertEquals(1, result.size());
        assertEquals(txn.getId(), result.get(0).getId());
        assertEquals(txn.getType(), result.get(0).getType());
        assertEquals(txn.getQuantity(), result.get(0).getQuantity());
        assertEquals(txn.getPrice(), result.get(0).getPrice());
        assertEquals(txn.getCurrency(), result.get(0).getCurrency());
        assertEquals(txn.getFxRateToHome(), result.get(0).getFxRateToHome());
        assertEquals(txn.getTxnDate(), result.get(0).getTxnDate());
        assertEquals(txn.getCreatedAt(), result.get(0).getCreatedAt());
    }

    @Test
    void getTransactionsByInvestmentId_EmptyList() {
        UUID investmentId = UUID.randomUUID();
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId))
                .thenReturn(List.of());

        List<TransactionResponse> result = transactionService.getTransactionsByInvestmentId(investmentId);
        assertTrue(result.isEmpty());
    }

    @Test
    void createTransaction_ValidBuy_SavesAndReturns() {
        UUID investmentId = UUID.randomUUID();
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(TransactionType.BUY);
        request.setQuantity(new BigDecimal("10"));
        request.setPrice(new BigDecimal("150.00"));
        request.setCurrency("USD");
        request.setFxRateToHome(new BigDecimal("1.0"));
        request.setTxnDate(LocalDate.now().minusDays(1));

        Transaction saved = buildTransaction(investmentId, TransactionType.BUY, "10.00000000", "150.00000000");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.createTransaction(investmentId, request, "USD");

        assertNotNull(response);
        assertEquals(saved.getId(), response.getId());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void createTransaction_FutureDated_ThrowsIllegalArgument() {
        UUID investmentId = UUID.randomUUID();
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(TransactionType.BUY);
        request.setQuantity(new BigDecimal("5"));
        request.setPrice(new BigDecimal("100"));
        request.setTxnDate(LocalDate.now().plusDays(5));

        assertThrows(IllegalArgumentException.class,
            () -> transactionService.createTransaction(investmentId, request, "USD"));
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void createTransaction_SellExceedsHoldings_ThrowsIllegalArgument() {
        UUID investmentId = UUID.randomUUID();

        // Existing holding: 5 units
        Transaction existing = buildTransaction(investmentId, TransactionType.BUY, "5", "100");
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId))
                .thenReturn(List.of(existing));

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(TransactionType.SELL);
        request.setQuantity(new BigDecimal("10")); // more than 5
        request.setPrice(new BigDecimal("120"));
        request.setTxnDate(LocalDate.now().minusDays(1));

        assertThrows(IllegalArgumentException.class,
            () -> transactionService.createTransaction(investmentId, request, "USD"));
        verify(transactionRepository, never()).save(any());
    }

        @Test
        void createTransaction_SellBeforeBuyDate_ThrowsIllegalArgument() {
        UUID investmentId = UUID.randomUUID();

        Transaction futureBuy = buildTransaction(investmentId, TransactionType.BUY, "10", "100");
        futureBuy.setTxnDate(LocalDate.now().plusDays(2));
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId))
            .thenReturn(List.of(futureBuy));

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(TransactionType.SELL);
        request.setQuantity(new BigDecimal("1"));
        request.setPrice(new BigDecimal("120"));
        request.setTxnDate(LocalDate.now());

        assertThrows(IllegalArgumentException.class,
            () -> transactionService.createTransaction(investmentId, request, "USD"));
        verify(transactionRepository, never()).save(any());
        }

    @Test
    void createTransaction_ValidSell_Succeeds() {
        UUID investmentId = UUID.randomUUID();

        Transaction existing = buildTransaction(investmentId, TransactionType.BUY, "10", "100");
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId))
                .thenReturn(List.of(existing));

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(TransactionType.SELL);
        request.setQuantity(new BigDecimal("5"));
        request.setPrice(new BigDecimal("120"));
        request.setCurrency("USD");
        request.setFxRateToHome(new BigDecimal("1.0"));
        request.setTxnDate(LocalDate.now().minusDays(1));

        Transaction saved = buildTransaction(investmentId, TransactionType.SELL, "5.00000000", "120.00000000");
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        TransactionResponse response = transactionService.createTransaction(investmentId, request, "USD");
        assertNotNull(response);
        assertEquals(TransactionType.SELL, response.getType());
    }

    @Test
    void createTransaction_FillsMissingFxRateFromService() {
        UUID investmentId = UUID.randomUUID();

        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(TransactionType.BUY);
        request.setQuantity(new BigDecimal("10"));
        request.setPrice(new BigDecimal("150.00"));
        request.setCurrency("EUR");
        request.setTxnDate(LocalDate.now().minusDays(1));

        when(fxRateService.getRate(eq("EUR"), eq("USD"), any(LocalDate.class)))
                .thenReturn(Optional.of(new BigDecimal("1.10000000")));

        Transaction saved = buildTransaction(investmentId, TransactionType.BUY, "10.00000000", "150.00000000");
        saved.setCurrency("EUR");
        saved.setFxRateToHome(new BigDecimal("1.10000000"));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        transactionService.createTransaction(investmentId, request, "USD");

        verify(transactionRepository).save(captor.capture());
        assertEquals(new BigDecimal("1.10000000"), captor.getValue().getFxRateToHome());
    }

    @Test
    void deleteTransaction_NotFound_ThrowsResourceNotFound() {
        UUID id = UUID.randomUUID();
        when(transactionRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> transactionService.deleteTransaction(id));
        verify(transactionRepository, never()).delete(any());
    }

    @Test
    void deleteTransaction_Exists_Deletes() {
        UUID id = UUID.randomUUID();
        Transaction txn = new Transaction();
        txn.setId(id);
        when(transactionRepository.findById(id)).thenReturn(Optional.of(txn));

        transactionService.deleteTransaction(id);

        verify(transactionRepository).delete(txn);
    }

    private Transaction buildTransaction(UUID investmentId, TransactionType type, String qty, String price) {
        Transaction txn = new Transaction();
        txn.setId(UUID.randomUUID());
        txn.setInvestmentId(investmentId);
        txn.setType(type);
        txn.setQuantity(new BigDecimal(qty));
        txn.setPrice(new BigDecimal(price));
        txn.setCurrency("USD");
        txn.setFxRateToHome(new BigDecimal("1.0"));
        txn.setTxnDate(LocalDate.now().minusDays(1));
        txn.setCreatedAt(Instant.now());
        return txn;
    }
}
