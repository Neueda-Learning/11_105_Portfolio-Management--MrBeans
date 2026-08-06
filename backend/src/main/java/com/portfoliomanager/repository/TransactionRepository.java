package com.portfoliomanager.repository;

import com.portfoliomanager.model.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Optional<Transaction> findById(UUID id);

    Transaction save(Transaction transaction);

    List<Transaction> saveAll(List<Transaction> transactions);

    void delete(Transaction transaction);

    // Ordered by txn_date ascending per indexing checklist (V1 schema index)
    List<Transaction> findByInvestmentIdOrderByTxnDateAsc(UUID investmentId);
}