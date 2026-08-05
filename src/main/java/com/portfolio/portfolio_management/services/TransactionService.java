package com.portfolio.portfolio_management.services;

import com.portfolio.portfolio_management.dto.TransactionRequestDTO;
import com.portfolio.portfolio_management.dto.TransactionResponseDTO;

import java.util.List;

public interface TransactionService {
    List<TransactionResponseDTO> getAllTransactions();

    TransactionResponseDTO getTransactionById(Long id);

    TransactionResponseDTO createTransaction(Long investmentId, TransactionRequestDTO request);

    TransactionResponseDTO update(Long id, TransactionRequestDTO request);

    void deleteTransaction(Long id);
}
