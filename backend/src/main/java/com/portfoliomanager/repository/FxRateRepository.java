package com.portfoliomanager.repository;

import com.portfoliomanager.model.FxRate;

import java.time.LocalDate;
import java.util.Optional;

public interface FxRateRepository {
    FxRate save(FxRate fxRate);

    Optional<FxRate> findByFromCurrencyAndToCurrencyAndRateDate(String fromCurrency, String toCurrency, LocalDate rateDate);

    /** Returns the most recently stored rate for the given pair, regardless of date. */
    Optional<FxRate> findMostRecentByFromCurrencyAndToCurrency(String fromCurrency, String toCurrency);
}