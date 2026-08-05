package com.portfolio.portfolio_management.controller;

import com.portfolio.portfolio_management.dto.TransactionRequestDTO;
import com.portfolio.portfolio_management.dto.TransactionResponseDTO;
import com.portfolio.portfolio_management.services.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // GET ALL TRANSACTIONS
    @GetMapping
    public List<TransactionResponseDTO> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    // GET TRANSACTION BY ID
    @GetMapping("/{id}")
    public TransactionResponseDTO getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    // CREATE TRANSACTION
    @PostMapping("/investment/{investmentId}")
    public TransactionResponseDTO createTransaction(@PathVariable Long investmentId,
                                                    @RequestBody TransactionRequestDTO request) {
        return transactionService.createTransaction(investmentId, request);
    }

    // UPDATE TRANSACTION
    @PutMapping("/{id}")
    public TransactionResponseDTO updateTransaction(@PathVariable Long id,
                                                    @RequestBody TransactionRequestDTO request) {
        return transactionService.update(id, request);
    }

    // DELETE TRANSACTION
    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}