package com.portfoliomanager.controller;

import com.portfoliomanager.dto.transaction.CreateTransactionRequest;
import com.portfoliomanager.dto.transaction.TransactionResponse;
import com.portfoliomanager.model.TransactionType;
import com.portfoliomanager.service.TransactionService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock private TransactionService transactionService;

    private TransactionController controller;

    @BeforeEach
    void setUp() {
        controller = new TransactionController(transactionService);
    }

    @Test
    void getTransactionsByInvestmentId_DelegatesToService() {
        UUID investmentId = UUID.randomUUID();
        TransactionResponse resp = new TransactionResponse();
        resp.setType(TransactionType.BUY);
        when(transactionService.getTransactionsByInvestmentId(investmentId))
                .thenReturn(List.of(resp));

        List<TransactionResponse> result = controller.getTransactionsByInvestmentId(investmentId);

        assertEquals(1, result.size());
        assertEquals(TransactionType.BUY, result.get(0).getType());
    }

    @Test
    void createTransaction_DelegatesToService() {
        UUID investmentId = UUID.randomUUID();
        CreateTransactionRequest request = new CreateTransactionRequest();
        TransactionResponse resp = new TransactionResponse();
        resp.setInvestmentId(investmentId);
        when(transactionService.createTransaction(investmentId, request)).thenReturn(resp);

        TransactionResponse result = controller.createTransaction(investmentId, request);
        assertEquals(investmentId, result.getInvestmentId());
    }

    @Test
    void deleteTransaction_DelegatesToService() {
        UUID investmentId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        controller.deleteTransaction(investmentId, id);
        verify(transactionService).deleteTransaction(id);
    }
}
