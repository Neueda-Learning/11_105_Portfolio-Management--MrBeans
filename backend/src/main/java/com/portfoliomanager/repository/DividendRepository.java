package com.portfoliomanager.repository;

import com.portfoliomanager.model.Dividend;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DividendRepository extends JpaRepository<Dividend, UUID> {
    List<Dividend> findByInvestmentId(UUID investmentId);
    List<Dividend> findByInvestmentIdOrderByPaymentDateDesc(UUID investmentId);
    void deleteByInvestmentId(UUID investmentId);

    @Query("SELECT COALESCE(SUM(d.amount - d.withholdingTax), 0) FROM Dividend d WHERE d.paymentDate BETWEEN :start AND :end")
    Optional<BigDecimal> sumNetAmountByPaymentDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
