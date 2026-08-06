package com.portfoliomanager.repository;

import com.portfoliomanager.model.Investment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvestmentRepository {
	List<Investment> findAll();

	Optional<Investment> findById(UUID id);

	Investment save(Investment investment);

	void delete(Investment investment);
}