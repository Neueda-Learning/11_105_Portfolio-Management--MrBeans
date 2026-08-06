package com.portfoliomanager.repository;

import com.portfoliomanager.BaseIntegrationTest;
import com.portfoliomanager.model.FxRate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FxRateRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private FxRateRepository fxRateRepository;

    @Test
    void canSaveAndRetrieveFxRate() {
        FxRate rate = new FxRate();
        rate.setFromCurrency("USD");
        rate.setToCurrency("INR");
        rate.setRate(new BigDecimal("83.5"));
        rate.setRateDate(LocalDate.of(2023, 10, 1));
        
        fxRateRepository.save(rate);

        Optional<FxRate> retrieved = fxRateRepository.findByFromCurrencyAndToCurrencyAndRateDate(
                "USD", "INR", LocalDate.of(2023, 10, 1)
        );
        
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getRate()).isEqualByComparingTo("83.5");
    }
}
