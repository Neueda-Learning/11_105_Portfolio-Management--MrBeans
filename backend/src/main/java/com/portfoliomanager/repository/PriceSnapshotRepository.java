package com.portfoliomanager.repository;

import com.portfoliomanager.model.PriceSnapshot;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface PriceSnapshotRepository extends JpaRepository<PriceSnapshot, UUID> {
    
    // Uses V1 schema index idx_price_snapshots_inv_fetched
    List<PriceSnapshot> findByInvestmentIdOrderByFetchedAtDesc(UUID investmentId);
}
