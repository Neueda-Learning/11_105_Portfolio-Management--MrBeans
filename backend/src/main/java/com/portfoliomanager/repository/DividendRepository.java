package com.portfoliomanager.repository;

import com.portfoliomanager.model.Dividend;

import java.util.List;
import java.util.UUID;

public interface DividendRepository {
    Dividend save(Dividend dividend);

    List<Dividend> findByInvestmentId(UUID investmentId);
}