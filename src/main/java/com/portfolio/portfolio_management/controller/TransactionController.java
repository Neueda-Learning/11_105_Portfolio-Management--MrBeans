package com.portfolio.portfolio_management.controller;

import com.portfolio.portfolio_management.entity.Transaction;
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
    public List<Transaction> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    // GET TRANSACTION BY ID
    @GetMapping("/{id}")
    public Transaction getTransactionById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    // CREATE TRANSACTION
    @PostMapping("/investment/{investmentId}")
    public Transaction createTransaction(@PathVariable Long investmentId,
                                         @RequestBody Transaction transaction) {
        return transactionService.createTransaction(investmentId, transaction);
    }

    // UPDATE TRANSACTION
    @PutMapping("/{id}")
    public Transaction updateTransaction(@PathVariable Long id,
                                         @RequestBody Transaction transaction) {
        return transactionService.update(id, transaction);
    }

    // DELETE TRANSACTION
    @DeleteMapping("/{id}")
    public void deleteTransaction(@PathVariable Long id) {
        transactionService.deleteTransaction(id);
    }
}