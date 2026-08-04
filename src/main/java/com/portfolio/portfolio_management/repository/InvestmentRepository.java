package com.portfolio.portfolio_management.repository;

import com.portfolio.portfolio_management.entity.Investment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InvestmentRepository extends JpaRepository<Investment, Long> {
}
