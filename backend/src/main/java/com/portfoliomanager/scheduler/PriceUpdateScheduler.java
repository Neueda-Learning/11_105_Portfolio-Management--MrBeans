package com.portfoliomanager.scheduler;

import com.portfoliomanager.client.PriceFeedClient;
import com.portfoliomanager.service.PriceSnapshotService;

import com.portfoliomanager.model.Investment;
import com.portfoliomanager.repository.InvestmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PriceUpdateScheduler {

    private static final Logger log = LoggerFactory.getLogger(PriceUpdateScheduler.class);

    private final InvestmentRepository investmentRepository;
    private final PriceFeedClient priceFeedClient;
    private final PriceSnapshotService priceSnapshotService;

    public PriceUpdateScheduler(InvestmentRepository investmentRepository,
                                PriceFeedClient priceFeedClient,
                                PriceSnapshotService priceSnapshotService) {
        this.investmentRepository = investmentRepository;
        this.priceFeedClient = priceFeedClient;
        this.priceSnapshotService = priceSnapshotService;
    }

    /**
     * Scheduled to run hourly by default. Overridable via application.yml.
     * Batches all symbols into a single request. 
     * Handles failures gracefully per PRD Section 3.3.
     */
    @Scheduled(fixedRateString = "${portfolio.update-frequency-ms:3600000}")
    public void updatePrices() {
        log.info("Starting scheduled price update cycle");
        
        List<Investment> investments = investmentRepository.findAll();
        if (investments.isEmpty()) {
            log.info("No investments found to update");
            return;
        }

        // Get distinct symbols to avoid requesting the same ticker multiple times in the batch
        List<String> symbols = investments.stream()
                .map(Investment::getSymbol)
                .distinct()
                .collect(Collectors.toList());

        Map<String, PriceFeedClient.CurrentPrice> prices;
        try {
            prices = priceFeedClient.getPrices(symbols);
        } catch (Exception e) {
            // Log and skip entire cycle if the batch call fundamentally fails (e.g. network partition)
            log.error("Failed to fetch batched prices from provider. Skipping this update cycle.", e);
            return;
        }

        // Process each investment individually so one bad DB save or missing ticker doesn't crash the loop
        for (Investment investment : investments) {
            try {
                PriceFeedClient.CurrentPrice currentPrice = prices.get(investment.getSymbol());
                if (currentPrice == null) {
                    log.warn("No price data returned for symbol: {}. Skipping.", investment.getSymbol());
                    continue;
                }
                
                priceSnapshotService.saveSnapshot(
                        investment.getId(),
                        currentPrice.price(),
                        currentPrice.currency(),
                        currentPrice.fetchedAt()
                );
            } catch (Exception e) {
                log.error("Failed to process and save price snapshot for symbol: {}", investment.getSymbol(), e);
            }
        }
        
        log.info("Completed scheduled price update cycle");
    }
}
