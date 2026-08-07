package com.portfoliomanager.service;

import com.portfoliomanager.model.PriceSnapshot;
import com.portfoliomanager.repository.PriceSnapshotRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PriceSnapshotServiceTest {

    @Mock
    private PriceSnapshotRepository priceSnapshotRepository;

    private PriceSnapshotService priceSnapshotService;

    @BeforeEach
    void setUp() {
        priceSnapshotService = new PriceSnapshotService(priceSnapshotRepository);
    }

    @Test
    void saveSnapshot_DelegatesToRepositoryWithCorrectFields() {
        UUID investmentId = UUID.randomUUID();
        BigDecimal price = new BigDecimal("150.50");
        String currency = "USD";
        Instant fetchedAt = Instant.now();

        PriceSnapshot expected = new PriceSnapshot();
        expected.setId(UUID.randomUUID());
        expected.setInvestmentId(investmentId);
        expected.setPrice(price);
        expected.setCurrency(currency);
        expected.setFetchedAt(fetchedAt);

        when(priceSnapshotRepository.save(any(PriceSnapshot.class))).thenReturn(expected);

        PriceSnapshot result = priceSnapshotService.saveSnapshot(investmentId, price, currency, fetchedAt);

        assertNotNull(result);
        assertEquals(expected.getId(), result.getId());

        ArgumentCaptor<PriceSnapshot> captor = ArgumentCaptor.forClass(PriceSnapshot.class);
        verify(priceSnapshotRepository).save(captor.capture());

        PriceSnapshot captured = captor.getValue();
        assertEquals(investmentId, captured.getInvestmentId());
        assertEquals(price, captured.getPrice());
        assertEquals(currency, captured.getCurrency());
        assertEquals(fetchedAt, captured.getFetchedAt());
    }
}
