package com.portfolio.portfolio_management.services.impl;

import com.portfolio.portfolio_management.dto.TransactionRequestDTO;
import com.portfolio.portfolio_management.dto.TransactionResponseDTO;
import com.portfolio.portfolio_management.entity.Investment;
import com.portfolio.portfolio_management.entity.Transaction;
import com.portfolio.portfolio_management.repository.InvestmentRepository;
import com.portfolio.portfolio_management.repository.TransactionRepository;
import com.portfolio.portfolio_management.services.TransactionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionServiceImpl implements TransactionService {
    private final InvestmentRepository investmentRepository;
    private final TransactionRepository transactionRepository;

    public TransactionServiceImpl(InvestmentRepository investmentRepository,
                                  TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
        this.investmentRepository = investmentRepository;
    }

    private TransactionResponseDTO toResponseDTO(Transaction transaction) {
        TransactionResponseDTO dto = new TransactionResponseDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setQuantity(transaction.getQuantity());
        dto.setPricePerUnit(transaction.getPricePerUnit());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setNotes(transaction.getNotes());
        dto.setInvestmentId(transaction.getInvestment().getInvestmentId());
        return dto;
    }

    private void applyRequest(Transaction transaction, TransactionRequestDTO request) {
        transaction.setTransactionType(request.getTransactionType());
        transaction.setQuantity(request.getQuantity());
        transaction.setPricePerUnit(request.getPricePerUnit());
        transaction.setTransactionDate(request.getTransactionDate());
        transaction.setNotes(request.getNotes());
    }

    @Override
    public List<TransactionResponseDTO> getAllTransactions() {
        return transactionRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public TransactionResponseDTO getTransactionById(Long id) {
        return transactionRepository.findById(id)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    @Override
    public TransactionResponseDTO createTransaction(Long investmentId, TransactionRequestDTO request) {
        Investment investment = investmentRepository.findById(investmentId).orElse(null);
        if (investment == null) return null;

        Transaction transaction = new Transaction();
        applyRequest(transaction, request);
        transaction.setInvestment(investment);
        return toResponseDTO(transactionRepository.save(transaction));
    }

    @Override
    public TransactionResponseDTO update(Long id, TransactionRequestDTO request) {
        return transactionRepository.findById(id).map(existing -> {
            applyRequest(existing, request);
            return toResponseDTO(transactionRepository.save(existing));
        }).orElse(null);
    }

    @Override
    public void deleteTransaction(Long id) {
        transactionRepository.deleteById(id);
    }
}
