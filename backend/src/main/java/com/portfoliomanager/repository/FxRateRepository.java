package com.portfoliomanager.repository;

import com.portfoliomanager.model.FxRate;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface FxRateRepository extends JpaRepository<FxRate, UUID> {
    
    Optional<FxRate> findByFromCurrencyAndToCurrencyAndRateDate(String fromCurrency, String toCurrency, LocalDate rateDate);
}
