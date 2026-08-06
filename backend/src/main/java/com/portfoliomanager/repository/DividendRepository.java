package com.portfoliomanager.repository;

import com.portfoliomanager.model.Dividend;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface DividendRepository extends JpaRepository<Dividend, UUID> {
    List<Dividend> findByInvestmentId(UUID investmentId);
}
