package com.portfoliomanager.service;

import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.model.Investment;

import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.dto.investment.CreateInvestmentRequest;
import com.portfoliomanager.dto.investment.UpdateInvestmentRequest;
import com.portfoliomanager.dto.investment.InvestmentResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class InvestmentService {

    private final InvestmentRepository investmentRepository;

    public InvestmentService(InvestmentRepository investmentRepository) {
        this.investmentRepository = investmentRepository;
    }

    @Transactional(readOnly = true)
    public List<InvestmentResponse> getAllInvestments() {
        return investmentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InvestmentResponse getInvestmentById(UUID id) {
        Investment investment = getInvestmentEntity(id);
        return mapToResponse(investment);
    }

    @Transactional
    public InvestmentResponse createInvestment(CreateInvestmentRequest request) {
        Investment investment = new Investment();
        investment.setSymbol(request.getSymbol());
        investment.setName(request.getName());
        investment.setType(request.getType());
        investment.setCurrency(request.getCurrency());
        investment.setMetadata(request.getMetadata());

        Investment saved = investmentRepository.save(investment);
        return mapToResponse(saved);
    }

    @Transactional
    public InvestmentResponse updateInvestment(UUID id, UpdateInvestmentRequest request) {
        Investment investment = getInvestmentEntity(id);
        
        if (request.getSymbol() != null) investment.setSymbol(request.getSymbol());
        if (request.getName() != null) investment.setName(request.getName());
        if (request.getType() != null) investment.setType(request.getType());
        if (request.getCurrency() != null) investment.setCurrency(request.getCurrency());
        if (request.getMetadata() != null) investment.setMetadata(request.getMetadata());

        Investment updated = investmentRepository.save(investment);
        return mapToResponse(updated);
    }

    @Transactional
    public void deleteInvestment(UUID id) {
        Investment investment = getInvestmentEntity(id);
        investmentRepository.delete(investment);
    }

    private Investment getInvestmentEntity(UUID id) {
        return investmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Investment not found with id " + id));
    }

    private InvestmentResponse mapToResponse(Investment investment) {
        InvestmentResponse response = new InvestmentResponse();
        response.setId(investment.getId());
        response.setSymbol(investment.getSymbol());
        response.setName(investment.getName());
        response.setType(investment.getType());
        response.setCurrency(investment.getCurrency());
        response.setMetadata(investment.getMetadata());
        response.setCreatedAt(investment.getCreatedAt());
        response.setUpdatedAt(investment.getUpdatedAt());
        return response;
    }
}
