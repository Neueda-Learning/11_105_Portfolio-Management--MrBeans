package com.portfoliomanager.service;

import com.portfoliomanager.dto.dashboard.AllocationResponse;
import com.portfoliomanager.dto.dashboard.DashboardSummaryResponse;
import com.portfoliomanager.dto.dashboard.TrendResponse;
import com.portfoliomanager.dto.pnl.InvestmentPnlResponse;
import com.portfoliomanager.exception.ResourceNotFoundException;
import com.portfoliomanager.model.Investment;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.PriceSnapshot;
import com.portfoliomanager.model.Transaction;
import com.portfoliomanager.model.TransactionType;
import com.portfoliomanager.repository.DividendRepository;
import com.portfoliomanager.repository.InvestmentRepository;
import com.portfoliomanager.repository.PriceSnapshotRepository;
import com.portfoliomanager.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PriceSnapshotRepository priceSnapshotRepository;

    @Mock
    private FxRateService fxRateService;

    @Mock
    private DividendRepository dividendRepository;

    @InjectMocks
    private DashboardService dashboardService;

    private UUID stockId;
    private UUID bondId;
    private Investment stock;
    private Investment bond;

    @BeforeEach
    void setUp() {
        stockId = UUID.randomUUID();
        bondId = UUID.randomUUID();

        stock = investment(stockId, InvestmentType.STOCK, "USD");
        bond = investment(bondId, InvestmentType.BOND, "EUR");
    }

    @Test
    void getSummary_AggregatesPnlAndDividendIncome() {
        LocalDate today = LocalDate.now();

        when(investmentRepository.findAll()).thenReturn(List.of(stock));
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(stockId))
                .thenReturn(List.of(buyTxn(stockId, today.minusDays(3), "2", "100")));
        when(priceSnapshotRepository.findByInvestmentIdOrderByFetchedAtDesc(stockId))
                .thenReturn(sortedDescSnapshots(List.of(
                        snapshot(stockId, today.plusDays(1), "130"),
                        snapshot(stockId, today.minusDays(1), "120")
                )));
        when(fxRateService.getRate(eq("USD"), eq("INR"), any(LocalDate.class)))
                .thenReturn(Optional.of(BigDecimal.ONE));
        when(dividendRepository.sumNetAmountByPaymentDateBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Optional.of(new BigDecimal("12.345")));

        DashboardSummaryResponse response = dashboardService.getSummary("INR");

        assertEquals(new BigDecimal("200.00"), response.getTotalCostBasis());
        assertEquals(new BigDecimal("0.00"), response.getTotalRealisedPnl());
        assertEquals(new BigDecimal("40.00"), response.getTotalUnrealisedPnl());
        assertEquals(new BigDecimal("240.00"), response.getTotalValue());
        assertEquals(new BigDecimal("12.35"), response.getDividendIncomeThisYear());
    }

    @Test
    void getAllocation_CalculatesPerTypePercentages() {
        LocalDate today = LocalDate.now();

        when(investmentRepository.findAll()).thenReturn(List.of(stock, bond));
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(stockId))
                .thenReturn(List.of(buyTxn(stockId, today.minusDays(5), "1", "100")));
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(bondId))
                .thenReturn(List.of(buyTxn(bondId, today.minusDays(5), "1", "90")));
        when(priceSnapshotRepository.findByInvestmentIdOrderByFetchedAtDesc(stockId))
                .thenReturn(List.of(snapshot(stockId, today.minusDays(1), "110")));
        when(priceSnapshotRepository.findByInvestmentIdOrderByFetchedAtDesc(bondId))
                .thenReturn(List.of(snapshot(bondId, today.minusDays(1), "90")));
        when(fxRateService.getRate(any(String.class), eq("INR"), any(LocalDate.class)))
                .thenReturn(Optional.of(BigDecimal.ONE));

        List<AllocationResponse> allocations = dashboardService.getAllocation("INR");

        AllocationResponse stockAllocation = allocations.stream()
                .filter(a -> a.getType() == InvestmentType.STOCK)
                .findFirst()
                .orElseThrow();

        AllocationResponse bondAllocation = allocations.stream()
                .filter(a -> a.getType() == InvestmentType.BOND)
                .findFirst()
                .orElseThrow();

        assertEquals(new BigDecimal("110.00"), stockAllocation.getTotalValue());
        assertEquals(new BigDecimal("55.00"), stockAllocation.getPercentage());
        assertEquals(new BigDecimal("90.00"), bondAllocation.getTotalValue());
        assertEquals(new BigDecimal("45.00"), bondAllocation.getPercentage());
    }

    @Test
    void getTrendFiltered_RejectsInvalidArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getTrendFiltered("INR", null, null, null, 0));

        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getTrendFiltered("INR", LocalDate.now().minusDays(1), null, null, 5));

        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getTrendFiltered("INR", LocalDate.now(), LocalDate.now().minusDays(1), null, 5));

        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getTrendFiltered(
                        "INR",
                        LocalDate.now().minusDays(731),
                        LocalDate.now(),
                        null,
                        5
                ));

        assertThrows(IllegalArgumentException.class,
                () -> dashboardService.getTrendFiltered("INR", null, null, null, 731));
    }

    @Test
    void getTrendFiltered_FiltersByTypeAndBuildsDailyPoints() {
        LocalDate today = LocalDate.now();

        when(investmentRepository.findAll()).thenReturn(List.of(stock, bond));
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(stockId))
                .thenReturn(List.of(buyTxn(stockId, today.minusDays(10), "1", "100")));
        when(priceSnapshotRepository.findByInvestmentIdOrderByFetchedAtDesc(stockId))
                .thenReturn(List.of(snapshot(stockId, today.minusDays(1), "100")));
        when(fxRateService.getLatestRate(eq("USD"), eq("INR")))
                .thenReturn(Optional.of(BigDecimal.ONE));

        List<TrendResponse> trend = dashboardService.getTrendFiltered(
                "INR",
                null,
                null,
                List.of(InvestmentType.STOCK),
                2
        );

        assertEquals(2, trend.size());
        assertEquals(new BigDecimal("100.00"), trend.get(0).getPortfolioValue());
        assertEquals(new BigDecimal("100.00"), trend.get(1).getPortfolioValue());

        verify(transactionRepository).findByInvestmentIdOrderByTxnDateAsc(stockId);
        verify(transactionRepository, never()).findByInvestmentIdOrderByTxnDateAsc(bondId);
        verify(priceSnapshotRepository, never()).findByInvestmentIdOrderByFetchedAtDesc(bondId);
    }

    @Test
    void getInvestmentPnl_ThrowsWhenInvestmentNotFound() {
        UUID missingId = UUID.randomUUID();
        when(investmentRepository.findById(missingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> dashboardService.getInvestmentPnl(missingId, "INR"));
    }

    @Test
    void getInvestmentPnl_ReturnsComputedValues() {
        LocalDate today = LocalDate.now();

        when(investmentRepository.findById(stockId)).thenReturn(Optional.of(stock));
        when(transactionRepository.findByInvestmentIdOrderByTxnDateAsc(stockId))
                .thenReturn(List.of(buyTxn(stockId, today.minusDays(3), "2", "100")));
        when(priceSnapshotRepository.findByInvestmentIdOrderByFetchedAtDesc(stockId))
                .thenReturn(List.of(snapshot(stockId, today.minusDays(1), "120")));
        when(fxRateService.getRate(eq("USD"), eq("INR"), any(LocalDate.class)))
                .thenReturn(Optional.of(BigDecimal.ONE));

        InvestmentPnlResponse response = dashboardService.getInvestmentPnl(stockId, "INR");

        assertEquals(new BigDecimal("0.00"), response.getRealisedPnl());
        assertEquals(new BigDecimal("40.00"), response.getUnrealisedPnl());
        assertEquals(new BigDecimal("200.00"), response.getTotalCostBasis());
        assertEquals(new BigDecimal("2.00000000"), response.getCurrentQuantity());
    }

    private static Investment investment(UUID id, InvestmentType type, String currency) {
        Investment investment = new Investment();
        investment.setId(id);
        investment.setType(type);
        investment.setCurrency(currency);
        investment.setSymbol("SYM-" + type.name());
        return investment;
    }

    private static Transaction buyTxn(UUID investmentId, LocalDate date, String qty, String price) {
        Transaction transaction = new Transaction();
        transaction.setInvestmentId(investmentId);
        transaction.setType(TransactionType.BUY);
        transaction.setQuantity(new BigDecimal(qty));
        transaction.setPrice(new BigDecimal(price));
        transaction.setCurrency("USD");
        transaction.setFxRateToHome(BigDecimal.ONE);
        transaction.setTxnDate(date);
        return transaction;
    }

    private static PriceSnapshot snapshot(UUID investmentId, LocalDate date, String price) {
        PriceSnapshot snapshot = new PriceSnapshot();
        snapshot.setInvestmentId(investmentId);
        snapshot.setCurrency("USD");
        snapshot.setPrice(new BigDecimal(price));
        snapshot.setFetchedAt(date.atTime(LocalTime.NOON).toInstant(ZoneOffset.UTC));
        return snapshot;
    }

    private static List<PriceSnapshot> sortedDescSnapshots(List<PriceSnapshot> snapshots) {
        return snapshots.stream()
                .sorted(Comparator.comparing(PriceSnapshot::getFetchedAt, Comparator.reverseOrder()))
                .collect(Collectors.toList());
    }
}
