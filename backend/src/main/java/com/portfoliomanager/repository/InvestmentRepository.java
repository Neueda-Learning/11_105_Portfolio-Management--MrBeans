package com.portfoliomanager.repository;

import com.portfoliomanager.model.Investment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface InvestmentRepository extends JpaRepository<Investment, UUID> {
}
