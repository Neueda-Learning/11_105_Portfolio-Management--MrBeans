package com.portfolio.portfolio_management.services;
import com.portfolio.portfolio_management.entity.Transaction;
import jakarta.persistence.Entity;

import java.util.List;

public interface TransactionService {
    List<Transaction> getAllTransactions();

    Transaction getTransactionById(Long id);

    Transaction createTransaction(Long investmentId ,Transaction transaction);

    Transaction update(Long id, Transaction transaction);

    void deleteTransaction(Long id);
}
