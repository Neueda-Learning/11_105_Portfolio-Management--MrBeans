package com.portfoliomanager.service;

import com.portfoliomanager.repository.TransactionRepository;
import com.portfoliomanager.model.Transaction;

import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.util.MoneyMath;
import com.portfoliomanager.dto.transaction.CreateTransactionRequest;
import com.portfoliomanager.dto.transaction.TransactionResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionsByInvestmentId(UUID investmentId) {
        return transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TransactionResponse createTransaction(UUID investmentId, CreateTransactionRequest request) {
        Transaction transaction = new Transaction();
        transaction.setInvestmentId(investmentId);
        transaction.setType(request.getType());
        transaction.setQuantity(MoneyMath.roundQuantity(request.getQuantity()));
        transaction.setPrice(MoneyMath.roundRate(request.getPrice()));
        transaction.setCurrency(request.getCurrency());
        transaction.setFxRateToHome(MoneyMath.roundQuantity(request.getFxRateToHome()));
        transaction.setTxnDate(request.getTxnDate());

        Transaction saved = transactionRepository.save(transaction);
        return mapToResponse(saved);
    }

    @Transactional
    public void deleteTransaction(UUID id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id " + id));
        transactionRepository.delete(transaction);
    }

    private TransactionResponse mapToResponse(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setInvestmentId(transaction.getInvestmentId());
        response.setType(transaction.getType());
        response.setQuantity(transaction.getQuantity());
        response.setPrice(transaction.getPrice());
        response.setCurrency(transaction.getCurrency());
        response.setFxRateToHome(transaction.getFxRateToHome());
        response.setTxnDate(transaction.getTxnDate());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
