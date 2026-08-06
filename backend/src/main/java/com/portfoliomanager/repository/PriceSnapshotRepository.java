package com.portfoliomanager.repository;

import com.portfoliomanager.model.PriceSnapshot;

import java.util.List;
import java.util.UUID;

public interface PriceSnapshotRepository {
    PriceSnapshot save(PriceSnapshot snapshot);

    // Uses V1 schema index idx_price_snapshots_inv_fetched
    List<PriceSnapshot> findByInvestmentIdOrderByFetchedAtDesc(UUID investmentId);
}
