package com.portfoliomanager.repository;

import com.portfoliomanager.model.Dividend;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DividendRepository {
    Dividend save(Dividend dividend);

    List<Dividend> findByInvestmentId(UUID investmentId);

    List<Dividend> findByInvestmentIdOrderByPaymentDateDesc(UUID investmentId);

    Optional<Dividend> findById(UUID id);

    void delete(Dividend dividend);

    void deleteByInvestmentId(UUID investmentId);

    void deleteAllInBatch();

    Optional<BigDecimal> sumNetAmountByPaymentDateBetween(LocalDate start, LocalDate end);
}
