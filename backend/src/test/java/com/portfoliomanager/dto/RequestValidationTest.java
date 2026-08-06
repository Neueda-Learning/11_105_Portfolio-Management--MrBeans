package com.portfoliomanager.dto;

import com.portfoliomanager.dto.chatbot.ChatRequest;
import com.portfoliomanager.dto.dashboard.AllocationResponse;
import com.portfoliomanager.dto.dashboard.TrendResponse;
import com.portfoliomanager.dto.dividend.CreateDividendRequest;
import com.portfoliomanager.dto.dividend.SimulateDividendRequest;
import com.portfoliomanager.dto.investment.CreateInvestmentRequest;
import com.portfoliomanager.dto.investment.UpdateInvestmentRequest;
import com.portfoliomanager.dto.transaction.CreateTransactionRequest;
import com.portfoliomanager.model.DividendMode;
import com.portfoliomanager.model.InvestmentType;
import com.portfoliomanager.model.TransactionType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestValidationTest {

    private static ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void closeValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void createInvestmentRequest_InvalidWhenRequiredFieldsMissingOrMalformed() {
        CreateInvestmentRequest request = new CreateInvestmentRequest();
        request.setSymbol("  ");
        request.setType(null);
        request.setCurrency("US");

        Set<String> fields = fieldsWithViolations(validator.validate(request));

        assertTrue(fields.contains("symbol"));
        assertTrue(fields.contains("type"));
        assertTrue(fields.contains("currency"));
    }

    @Test
    void createInvestmentRequest_ValidPayloadPassesValidation() {
        CreateInvestmentRequest request = new CreateInvestmentRequest();
        request.setSymbol("AAPL");
        request.setName("Apple Inc");
        request.setType(InvestmentType.STOCK);
        request.setCurrency("USD");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void updateInvestmentRequest_BlankValuesAndBadCurrencyAreRejected() {
        UpdateInvestmentRequest request = new UpdateInvestmentRequest();
        request.setSymbol("   ");
        request.setName("   ");
        request.setCurrency("US1");

        Set<String> fields = fieldsWithViolations(validator.validate(request));

        assertTrue(fields.contains("symbol"));
        assertTrue(fields.contains("name"));
        assertTrue(fields.contains("currency"));
    }

    @Test
    void createTransactionRequest_NegativeAndMissingFieldsAreRejected() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(null);
        request.setQuantity(new BigDecimal("-1"));
        request.setPrice(BigDecimal.ZERO);
        request.setCurrency("XX");
        request.setFxRateToHome(new BigDecimal("-0.1"));
        request.setTxnDate(null);

        Set<String> fields = fieldsWithViolations(validator.validate(request));

        assertTrue(fields.contains("type"));
        assertTrue(fields.contains("quantity"));
        assertTrue(fields.contains("price"));
        assertTrue(fields.contains("currency"));
        assertTrue(fields.contains("fxRateToHome"));
        assertTrue(fields.contains("txnDate"));
    }

    @Test
    void createTransactionRequest_ValidPayloadPassesValidation() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        request.setType(TransactionType.BUY);
        request.setQuantity(new BigDecimal("3.5"));
        request.setPrice(new BigDecimal("120.25"));
        request.setCurrency("USD");
        request.setFxRateToHome(BigDecimal.ONE);
        request.setTxnDate(LocalDate.now());

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void createTransactionRequest_GettersAndSettersWork() {
        CreateTransactionRequest request = new CreateTransactionRequest();
        LocalDate today = LocalDate.now();

        request.setType(TransactionType.SELL);
        request.setQuantity(new BigDecimal("4.25"));
        request.setPrice(new BigDecimal("88.40"));
        request.setCurrency("EUR");
        request.setFxRateToHome(new BigDecimal("1.10"));
        request.setTxnDate(today);

        assertEquals(TransactionType.SELL, request.getType());
        assertEquals(new BigDecimal("4.25"), request.getQuantity());
        assertEquals(new BigDecimal("88.40"), request.getPrice());
        assertEquals("EUR", request.getCurrency());
        assertEquals(new BigDecimal("1.10"), request.getFxRateToHome());
        assertEquals(today, request.getTxnDate());
    }

    @Test
    void createDividendRequest_InvalidFieldsAreRejected() {
        CreateDividendRequest request = new CreateDividendRequest();
        request.setAmount(new BigDecimal("-5"));
        request.setDividendPerShare(BigDecimal.ZERO);
        request.setCurrency(" ");
        request.setWithholdingTax(new BigDecimal("-1"));
        request.setReinvestmentPrice(BigDecimal.ZERO);
        request.setMode(null);
        request.setPaymentDate(null);

        Set<String> fields = fieldsWithViolations(validator.validate(request));

        assertTrue(fields.contains("amount"));
        assertTrue(fields.contains("dividendPerShare"));
        assertTrue(fields.contains("currency"));
        assertTrue(fields.contains("withholdingTax"));
        assertTrue(fields.contains("reinvestmentPrice"));
        assertTrue(fields.contains("mode"));
        assertTrue(fields.contains("paymentDate"));
    }

    @Test
    void simulateDividendRequest_InvalidFieldsAreRejected() {
        SimulateDividendRequest request = new SimulateDividendRequest();
        request.setDividendPerShare(BigDecimal.ZERO);
        request.setReinvestmentPrice(new BigDecimal("-1"));
        request.setMode(null);

        Set<String> fields = fieldsWithViolations(validator.validate(request));

        assertTrue(fields.contains("dividendPerShare"));
        assertTrue(fields.contains("reinvestmentPrice"));
        assertTrue(fields.contains("mode"));
    }

    @Test
    void simulateDividendRequest_ValidPayloadPassesValidation() {
        SimulateDividendRequest request = new SimulateDividendRequest();
        request.setDividendPerShare(new BigDecimal("1.25"));
        request.setReinvestmentPrice(new BigDecimal("99.50"));
        request.setMode(DividendMode.ACCUMULATIVE);

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void chatRequest_RejectsBlankAndOversizedMessage() {
        ChatRequest blankRequest = new ChatRequest();
        blankRequest.setMessage(" ");
        assertFalse(validator.validate(blankRequest).isEmpty());

        ChatRequest tooLongRequest = new ChatRequest();
        tooLongRequest.setMessage("a".repeat(2001));
        assertFalse(validator.validate(tooLongRequest).isEmpty());
    }

    @Test
    void chatRequest_GetterAndSetterWork() {
        ChatRequest request = new ChatRequest();
        request.setMessage("Hello from user");

        assertEquals("Hello from user", request.getMessage());
    }

    @Test
    void dashboardDto_AccessorsWork() {
        AllocationResponse allocation = new AllocationResponse(
                InvestmentType.STOCK,
                new BigDecimal("10.00"),
                new BigDecimal("50.00")
        );
        allocation.setType(InvestmentType.BOND);
        allocation.setTotalValue(new BigDecimal("12.00"));
        allocation.setPercentage(new BigDecimal("60.00"));

        assertEquals(InvestmentType.BOND, allocation.getType());
        assertEquals(new BigDecimal("12.00"), allocation.getTotalValue());
        assertEquals(new BigDecimal("60.00"), allocation.getPercentage());

        TrendResponse trend = new TrendResponse(LocalDate.now(), new BigDecimal("100.00"));
        LocalDate date = LocalDate.now().minusDays(1);
        trend.setDate(date);
        trend.setPortfolioValue(new BigDecimal("101.00"));

        assertEquals(date, trend.getDate());
        assertEquals(new BigDecimal("101.00"), trend.getPortfolioValue());
    }

    private static Set<String> fieldsWithViolations(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }
}
