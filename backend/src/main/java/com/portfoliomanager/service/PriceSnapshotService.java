package com.portfoliomanager.service;

import com.portfoliomanager.repository.PriceSnapshotRepository;
import com.portfoliomanager.model.PriceSnapshot;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class PriceSnapshotService {

    private final PriceSnapshotRepository priceSnapshotRepository;

    public PriceSnapshotService(PriceSnapshotRepository priceSnapshotRepository) {
        this.priceSnapshotRepository = priceSnapshotRepository;
    }

    @Transactional
    public PriceSnapshot saveSnapshot(UUID investmentId, BigDecimal price, String currency, Instant fetchedAt) {
        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setInvestmentId(investmentId);
        snapshot.setPrice(price);
        snapshot.setCurrency(currency);
        snapshot.setFetchedAt(fetchedAt);
        return priceSnapshotRepository.save(snapshot);
    }
}
