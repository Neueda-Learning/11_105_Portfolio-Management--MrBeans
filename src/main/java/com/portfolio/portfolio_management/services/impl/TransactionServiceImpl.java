package com.portfolio.portfolio_management.services.impl;

import com.portfolio.portfolio_management.entity.Investment;
import com.portfolio.portfolio_management.entity.Transaction;
import com.portfolio.portfolio_management.repository.InvestmentRepository;
import com.portfolio.portfolio_management.repository.TransactionRepository;
import com.portfolio.portfolio_management.services.TransactionService;
import jakarta.persistence.Entity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final InvestmentRepository investmentRepository;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(InvestmentRepository investmentRepository, TransactionRepository transactionRepository){
        this.transactionRepository = transactionRepository;
        this.investmentRepository = investmentRepository;


    }

    @Override
    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }

    @Override
    public Transaction getTransactionById(Long id) {
        return transactionRepository.findById(id).orElse(null);
    }


    @Override
    public Transaction createTransaction(Long investmentId, Transaction transaction) {
        Investment investment =
                investmentRepository.findById(investmentId).orElse(null);

        if (investment != null) {
            transaction.setInvestment(investment);
            return transactionRepository.save(transaction);
        }
        return null;
    }

    @Override
    public Transaction update(Long id, Transaction transaction) {
        Transaction existingTransaction =
                transactionRepository.findById(id).orElse(null);

        if (existingTransaction != null) {

            existingTransaction.setTransactionType(transaction.getTransactionType());
            existingTransaction.setQuantity(transaction.getQuantity());
            existingTransaction.setPricePerUnit(transaction.getPricePerUnit());
            existingTransaction.setTransactionDate(transaction.getTransactionDate());
            existingTransaction.setNotes(transaction.getNotes());

            return transactionRepository.save(existingTransaction);
        }
        return null;
    }

    @Override
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);

    }
}
