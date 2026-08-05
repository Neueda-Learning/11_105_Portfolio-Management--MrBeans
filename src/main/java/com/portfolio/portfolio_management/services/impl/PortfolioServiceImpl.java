package com.portfolio.portfolio_management.services.impl;

import com.portfolio.portfolio_management.dto.InvestmentResponseDTO;
import com.portfolio.portfolio_management.dto.PortfolioRequestDTO;
import com.portfolio.portfolio_management.dto.PortfolioResponseDTO;
import com.portfolio.portfolio_management.dto.TransactionResponseDTO;
import com.portfolio.portfolio_management.entity.Portfolio;
import com.portfolio.portfolio_management.repository.PortfolioRepository;
import com.portfolio.portfolio_management.services.PortfolioService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PortfolioServiceImpl implements PortfolioService {
    private final PortfolioRepository repository;

    public PortfolioServiceImpl(PortfolioRepository repository) {
        this.repository = repository;
    }

    private PortfolioResponseDTO toResponseDTO(Portfolio portfolio) {
        PortfolioResponseDTO dto = new PortfolioResponseDTO();
        dto.setPortfolioId(portfolio.getPortfolioId());
        dto.setPortfolioName(portfolio.getPortfolioName());
        dto.setInvestments(
                portfolio.getInvestments().stream()
                        .map(investment -> {
                            InvestmentResponseDTO invDto = new InvestmentResponseDTO();
                            invDto.setInvestmentId(investment.getInvestmentId());
                            invDto.setAssetName(investment.getAssetName());
                            invDto.setTickerSymbol(investment.getTickerSymbol());
                            invDto.setAssetType(investment.getAssetType());
                            invDto.setQuantity(investment.getQuantity());
                            invDto.setPurchasePrice(investment.getPurchasePrice());
                            invDto.setCurrentPrice(investment.getCurrentPrice());
                            invDto.setPurchaseDate(investment.getPurchaseDate());
                            invDto.setCurrency(investment.getCurrency());
                            invDto.setPortfolioId(portfolio.getPortfolioId());
                            invDto.setTransactions(
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
                            return invDto;
                        }).collect(Collectors.toList())
        );
        return dto;
    }

    @Override
    public List<PortfolioResponseDTO> getAllPortfolios() {
        return repository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PortfolioResponseDTO getPortfolioById(Long id) {
        return repository.findById(id)
                .map(this::toResponseDTO)
                .orElse(null);
    }

    @Override
    public PortfolioResponseDTO createPortfolio(PortfolioRequestDTO request) {
        Portfolio portfolio = new Portfolio();
        portfolio.setPortfolioName(request.getPortfolioName());
        return toResponseDTO(repository.save(portfolio));
    }

    @Override
    public PortfolioResponseDTO updatePortfolio(Long id, PortfolioRequestDTO request) {
        return repository.findById(id).map(existing -> {
            existing.setPortfolioName(request.getPortfolioName());
            return toResponseDTO(repository.save(existing));
        }).orElse(null);
    }

    @Override
    public void deletePortfolio(Long id) {
        repository.deleteById(id);
    }
}
