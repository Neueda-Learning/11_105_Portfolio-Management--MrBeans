package com.portfolio.portfolio_management.repository;

import com.portfolio.portfolio_management.entity.Portfolio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {


;

}
