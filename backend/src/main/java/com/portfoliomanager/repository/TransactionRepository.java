package com.portfoliomanager.repository;

import com.portfoliomanager.model.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    
    // Ordered by txn_date ascending per indexing checklist (V1 schema index)
    List<Transaction> findByInvestmentIdOrderByTxnDateAsc(UUID investmentId);
}
