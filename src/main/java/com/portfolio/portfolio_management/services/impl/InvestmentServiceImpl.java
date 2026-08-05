package com.portfolio.portfolio_management.services.impl;

import com.portfolio.portfolio_management.dto.InvestmentRequestDTO;
import com.portfolio.portfolio_management.dto.InvestmentResponseDTO;
import com.portfolio.portfolio_management.dto.TransactionResponseDTO;
import com.portfolio.portfolio_management.entity.Investment;
import com.portfolio.portfolio_management.entity.Portfolio;
import com.portfolio.portfolio_management.repository.InvestmentRepository;
import com.portfolio.portfolio_management.repository.PortfolioRepository;
import com.portfolio.portfolio_management.services.InvestmentService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvestmentServiceImpl implements InvestmentService {
    private final InvestmentRepository investmentRepository;
    private final PortfolioRepository portfolioRepository;

    public InvestmentServiceImpl(InvestmentRepository investmentRepository,
                                 PortfolioRepository portfolioRepository) {
        this.investmentRepository = investmentRepository;
        this.portfolioRepository = portfolioRepository;
    }

    private InvestmentResponseDTO toResponseDTO(Investment investment) {
        InvestmentResponseDTO dto = new InvestmentResponseDTO();
        dto.setInvestmentId(investment.getInvestmentId());
        dto.setAssetName(investment.getAssetName());
        dto.setTickerSymbol(investment.getTickerSymbol());
        dto.setAssetType(investment.getAssetType());
        dto.setQuantity(investment.getQuantity());
        dto.setPurchasePrice(investment.getPurchasePrice());
        dto.setCurrentPrice(investment.getCurrentPrice());
        dto.setPurchaseDate(investment.getPurchaseDate());
        dto.setCurrency(investment.getCurrency());
        dto.setPortfolioId(investment.getPortfolio().getPortfolioId());
        dto.setTransactions(
                investment.getTransactions().stream()
                        .map(t -> {
                            TransactionResponseDTO tDto = new TransactionResponseDTO();
                            tDto.setTransactionId(t.getTransactionId());
                            tDto.setTransactionType(t.getTransactionType());
                            tDto.setQuantity(t.getQuantity());
                            tDto.setPricePerUnit(t.getPricePerUnit());
                            tDto.setTransactionDate(t.getTransactionDate());
                            tDto.setNotes(t.getNotes());
                            tDto.setInvestmentId(investment.getInvestmentId());
                            return tDto;
                        }).collect(Collectors.toList())
        );
        return dto;
    }

    private void applyRequest(Investment investment, InvestmentRequestDTO request) {
        investment.setAssetName(request.getAssetName());
        investment.setTickerSymbol(request.getTickerSymbol());
        investment.setAssetType(request.getAssetType());
        investment.setQuantity(request.getQuantity());
        investment.setPurchasePrice(request.getPurchasePrice());
        investment.setCurrentPrice(request.getCurrentPrice());
        investment.setPurchaseDate(request.getPurchaseDate());
        investment.setCurrency(request.getCurrency());
    }

    @Override
    public List<InvestmentResponseDTO> getAllInvestments() {
        return investmentRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public InvestmentResponseDTO getInvestmentById(Long id) {
        return investmentRepository.findById(id)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    @Override
    public InvestmentResponseDTO createInvestment(Long portfolioId, InvestmentRequestDTO request) {
        Portfolio portfolio = portfolioRepository.findById(portfolioId).orElse(null);
        if (portfolio == null) return null;

        Investment investment = new Investment();
        applyRequest(investment, request);
        investment.setPortfolio(portfolio);
        return toResponseDTO(investmentRepository.save(investment));
    }

    @Override
    public InvestmentResponseDTO updateInvestment(Long id, InvestmentRequestDTO request) {
        return investmentRepository.findById(id).map(existing -> {
            applyRequest(existing, request);
            return toResponseDTO(investmentRepository.save(existing));
        }).orElse(null);
    }

    @Override
    public void deleteInvestment(Long id) {
        investmentRepository.deleteById(id);
    }
}
