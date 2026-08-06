package com.portfoliomanager.repository;

import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class TransactionRepositoryTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InvestmentRepository investmentRepository;

    private UUID investmentId;

    @BeforeEach
    void setUp() {
        // Need to save an investment first to satisfy the foreign key constraint
        Investment investment = new Investment();
        investment.setSymbol("AAPL");
        investment.setType(InvestmentType.STOCK);
        investment.setCurrency("USD");
        investment = investmentRepository.save(investment);
        investmentId = investment.getId();
    }

    @Test
    void findByInvestmentIdOrderByTxnDateAsc_ReturnsOrderedTransactions() {
        // Create transaction on day 3
        Transaction t3 = createTransaction(LocalDate.of(2023, 1, 3));
        // Create transaction on day 1
        Transaction t1 = createTransaction(LocalDate.of(2023, 1, 1));
        // Create transaction on day 2
        Transaction t2 = createTransaction(LocalDate.of(2023, 1, 2));

        transactionRepository.saveAll(List.of(t3, t1, t2));

        List<Transaction> results = transactionRepository.findByInvestmentIdOrderByTxnDateAsc(investmentId);

        assertEquals(3, results.size());
        assertEquals(LocalDate.of(2023, 1, 1), results.get(0).getTxnDate());
        assertEquals(LocalDate.of(2023, 1, 2), results.get(1).getTxnDate());
        assertEquals(LocalDate.of(2023, 1, 3), results.get(2).getTxnDate());
    }

    private Transaction createTransaction(LocalDate date) {
        Transaction tx = new Transaction();
        tx.setInvestmentId(investmentId);
        tx.setType(TransactionType.BUY);
        tx.setQuantity(new BigDecimal("10.0"));
        tx.setPrice(new BigDecimal("150.0"));
        tx.setTxnDate(date);
        return tx;
    }
}
