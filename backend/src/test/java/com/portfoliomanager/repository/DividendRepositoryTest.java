package com.portfoliomanager.repository;

import com.portfoliomanager.BaseIntegrationTest;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.Dividend;
import com.portfoliomanager.model.DividendMode;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DividendRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private DividendRepository dividendRepository;

    @Autowired
    private InvestmentRepository investmentRepository;

    @Test
    void canRetrieveDividendsByInvestmentId() {
        Investment inv = new Investment();
        inv.setSymbol("KO");
        inv.setType(InvestmentType.STOCK);
        inv.setCurrency("USD");
        investmentRepository.save(inv);

        Dividend div = new Dividend();
        div.setInvestmentId(inv.getId());
        div.setAmount(new BigDecimal("1.25"));
        div.setCurrency("USD");
        div.setMode(DividendMode.DISTRIBUTIVE);
        div.setPaymentDate(LocalDate.now());
        dividendRepository.save(div);

        List<Dividend> results = dividendRepository.findByInvestmentId(inv.getId());
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAmount()).isEqualByComparingTo("1.25");
    }
}
