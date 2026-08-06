package com.portfoliomanager.repository;

import com.portfoliomanager.BaseIntegrationTest;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentType;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class InvestmentRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private InvestmentRepository investmentRepository;

    @Test
    void canSaveAndRetrieveInvestmentWithMetadata() {
        Investment inv = new Investment();
        inv.setSymbol("AAPL");
        inv.setName("Apple Inc");
        inv.setType(InvestmentType.STOCK);
        inv.setCurrency("USD");
        inv.setMetadata(Map.of("sector", "Technology"));

        Investment saved = investmentRepository.save(inv);
        assertThat(saved.getId()).isNotNull();

        Optional<Investment> retrieved = investmentRepository.findById(saved.getId());
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getMetadata()).containsEntry("sector", "Technology");
    }
}
