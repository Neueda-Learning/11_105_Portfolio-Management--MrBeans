package com.portfoliomanager.controller;

import com.portfoliomanager.service.TransactionService;

import com.portfoliomanager.dto.transaction.CreateTransactionRequest;
import com.portfoliomanager.dto.transaction.TransactionResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments/{investmentId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionResponse> getTransactionsByInvestmentId(@PathVariable UUID investmentId) {
        return transactionService.getTransactionsByInvestmentId(investmentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponse createTransaction(
            @PathVariable UUID investmentId,
            @RequestParam(defaultValue = "INR")
            @Pattern(regexp = "^[A-Za-z]{3}$", message = "homeCurrency must be a 3-letter ISO code")
            String homeCurrency,
            @Valid @RequestBody CreateTransactionRequest request) {
        return transactionService.createTransaction(investmentId, request, homeCurrency);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTransaction(@PathVariable UUID investmentId, @PathVariable UUID id) {
        // investmentId is part of the path to maintain REST semantics, 
        // but we delete by transaction id directly.
        transactionService.deleteTransaction(id);
    }
}
