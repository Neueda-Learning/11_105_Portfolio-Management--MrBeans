package com.portfoliomanager.repository;

import com.portfoliomanager.BaseIntegrationTest;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.PriceSnapshot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PriceSnapshotRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private PriceSnapshotRepository priceSnapshotRepository;
    
    @Autowired
    private InvestmentRepository investmentRepository;

    @Test
    void returnsSnapshotsOrderedByFetchedAtDesc() {
        Investment inv = new Investment();
        inv.setSymbol("MSFT");
        inv.setType(InvestmentType.STOCK);
        inv.setCurrency("USD");
        investmentRepository.save(inv);

        PriceSnapshot older = new PriceSnapshot();
        older.setInvestmentId(inv.getId());
        older.setPrice(new BigDecimal("100"));
        older.setCurrency("USD");
        older.setFetchedAt(Instant.now().minus(2, ChronoUnit.DAYS));
        priceSnapshotRepository.save(older);

        PriceSnapshot newer = new PriceSnapshot();
        newer.setInvestmentId(inv.getId());
        newer.setPrice(new BigDecimal("110"));
        newer.setCurrency("USD");
        newer.setFetchedAt(Instant.now().minus(1, ChronoUnit.DAYS));
        priceSnapshotRepository.save(newer);

        List<PriceSnapshot> results = priceSnapshotRepository.findByInvestmentIdOrderByFetchedAtDesc(inv.getId());
        
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getPrice()).isEqualByComparingTo("110");
        assertThat(results.get(1).getPrice()).isEqualByComparingTo("100");
    }
}
