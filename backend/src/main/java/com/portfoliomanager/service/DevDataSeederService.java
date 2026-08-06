package com.portfoliomanager.service;

import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.PriceSnapshot;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;
import com.portfoliomanager.repository.DividendRepository;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.PriceSnapshotRepository;
import com.portfoliomanager.repository.TransactionRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@Profile("local")
public class DevDataSeederService {

    private final InvestmentRepository investmentRepository;
    private final TransactionRepository transactionRepository;
    private final PriceSnapshotRepository priceSnapshotRepository;
    private final DividendRepository dividendRepository;

    public DevDataSeederService(InvestmentRepository investmentRepository,
                                TransactionRepository transactionRepository,
                                PriceSnapshotRepository priceSnapshotRepository,
                                DividendRepository dividendRepository) {
        this.investmentRepository = investmentRepository;
        this.transactionRepository = transactionRepository;
        this.priceSnapshotRepository = priceSnapshotRepository;
        this.dividendRepository = dividendRepository;
    }

    @Transactional
    public SeedSummary seed(int investmentCount, int transactionsPerInvestment, int snapshotDays, boolean wipeExistingData) {
        if (wipeExistingData) {
            dividendRepository.deleteAllInBatch();
            priceSnapshotRepository.deleteAllInBatch();
            transactionRepository.deleteAllInBatch();
            investmentRepository.deleteAllInBatch();
        }

        LocalDate today = LocalDate.now();
        LocalDate txStartDate = today.minusDays(Math.max(snapshotDays, transactionsPerInvestment) + 30L);
        Random random = new Random(20260806L);

        int createdInvestments = 0;
        int createdTransactions = 0;
        int createdSnapshots = 0;

        InvestmentType[] investmentTypes = InvestmentType.values();

        for (int i = 0; i < investmentCount; i++) {
            Investment investment = new Investment();
            String symbol = String.format("SIM%03d", i + 1);
            investment.setSymbol(symbol);
            investment.setName("Simulated Asset " + (i + 1));
            investment.setType(investmentTypes[i % investmentTypes.length]);
            investment.setCurrency("USD");

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("seeded", true);
            metadata.put("bucket", investment.getType().name());
            investment.setMetadata(metadata);

            Investment savedInvestment = investmentRepository.save(investment);
            createdInvestments++;

            BigDecimal basePrice = bd(20 + random.nextDouble() * 380, 4);
            BigDecimal heldQty = BigDecimal.ZERO;

            for (int t = 0; t < transactionsPerInvestment; t++) {
                Transaction transaction = new Transaction();
                transaction.setInvestmentId(savedInvestment.getId());

                LocalDate txnDate = txStartDate.plusDays((long) t * 2 + random.nextInt(2));
                transaction.setTxnDate(txnDate);
                transaction.setCurrency("USD");
                transaction.setFxRateToHome(BigDecimal.ONE);

                boolean canSell = heldQty.compareTo(new BigDecimal("1")) > 0;
                boolean doBuy = !canSell || random.nextDouble() < 0.68;

                BigDecimal priceNoise = bd((random.nextDouble() - 0.5) * 0.10, 6);
                BigDecimal trend = bd((double) t / Math.max(1, transactionsPerInvestment) * 0.25, 6);
                BigDecimal price = basePrice
                        .multiply(BigDecimal.ONE.add(priceNoise).add(trend))
                        .max(new BigDecimal("1.0000"))
                        .setScale(4, RoundingMode.HALF_UP);

                if (doBuy) {
                    transaction.setType(TransactionType.BUY);
                    BigDecimal qty = bd(2 + random.nextDouble() * 18, 8);
                    transaction.setQuantity(qty);
                    transaction.setPrice(price);
                    heldQty = heldQty.add(qty);
                } else {
                    transaction.setType(TransactionType.SELL);
                    BigDecimal maxSell = heldQty.multiply(new BigDecimal("0.35"));
                    BigDecimal qty = bd(1 + random.nextDouble() * Math.max(1.0, maxSell.doubleValue()), 8);
                    if (qty.compareTo(heldQty) >= 0) {
                        qty = heldQty.multiply(new BigDecimal("0.5")).setScale(8, RoundingMode.HALF_UP);
                    }
                    transaction.setQuantity(qty);
                    transaction.setPrice(price);
                    heldQty = heldQty.subtract(qty);
                }

                transactionRepository.save(transaction);
                createdTransactions++;
            }

            for (int d = snapshotDays - 1; d >= 0; d--) {
                LocalDate snapshotDate = today.minusDays(d);
                double seasonal = Math.sin((snapshotDays - d) / 8.0) * 0.05;
                double drift = (snapshotDays - d) * 0.0009;
                double noise = (random.nextDouble() - 0.5) * 0.03;
                BigDecimal price = basePrice
                        .multiply(BigDecimal.valueOf(1 + seasonal + drift + noise))
                        .max(new BigDecimal("1.0000"))
                        .setScale(4, RoundingMode.HALF_UP);

                PriceSnapshot snapshot = new PriceSnapshot();
                snapshot.setInvestmentId(savedInvestment.getId());
                snapshot.setCurrency("USD");
                snapshot.setPrice(price);
                snapshot.setFetchedAt(toInstant(snapshotDate));
                priceSnapshotRepository.save(snapshot);
                createdSnapshots++;
            }
        }

        return new SeedSummary(createdInvestments, createdTransactions, createdSnapshots);
    }

    private static BigDecimal bd(double value, int scale) {
        return BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP);
    }

    private static Instant toInstant(LocalDate date) {
        return date.atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC);
    }

    public record SeedSummary(int investments, int transactions, int priceSnapshots) {}
}
