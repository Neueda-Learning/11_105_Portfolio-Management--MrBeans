package com.portfoliomanager.dto;

import com.portfoliomanager.dto.chatbot.ChatMessage;
import com.portfoliomanager.dto.chatbot.Role;
import com.portfoliomanager.dto.chatbot.ToolCallRequest;
import com.portfoliomanager.dto.dashboard.PerformanceResponse;
import com.portfoliomanager.dto.dividend.DividendResponse;
import com.portfoliomanager.dto.instrument.InstrumentSearchResult;
import com.portfoliomanager.dto.investment.InvestmentResponse;
import com.portfoliomanager.dto.pnl.InvestmentPnlResponse;
import com.portfoliomanager.dto.transaction.TransactionResponse;
import com.portfoliomanager.exception.ErrorResponse;
import com.portfoliomanager.model.*;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DtoGetterSetterTest {

    @Test
    void performanceResponse_GettersSetters() {
        PerformanceResponse r = new PerformanceResponse();
        r.setSymbol("AAPL");
        r.setName("Apple");
        r.setReturnPct(new BigDecimal("10.5"));
        r.setRiskScore(new BigDecimal("20.0"));
        r.setCurrentValue(new BigDecimal("5000"));
        r.setInvestmentType("STOCK");

        assertEquals("AAPL", r.getSymbol());
        assertEquals("Apple", r.getName());
        assertEquals(new BigDecimal("10.5"), r.getReturnPct());
        assertEquals(new BigDecimal("20.0"), r.getRiskScore());
        assertEquals(new BigDecimal("5000"), r.getCurrentValue());
        assertEquals("STOCK", r.getInvestmentType());
    }

    @Test
    void transactionResponse_GettersSetters() {
        TransactionResponse r = new TransactionResponse();
        UUID id = UUID.randomUUID();
        UUID investmentId = UUID.randomUUID();
        Instant now = Instant.now();

        r.setId(id);
        r.setInvestmentId(investmentId);
        r.setType(TransactionType.BUY);
        r.setQuantity(new BigDecimal("10"));
        r.setPrice(new BigDecimal("100"));
        r.setCurrency("USD");
        r.setFxRateToHome(new BigDecimal("1.0"));
        r.setTxnDate(LocalDate.now());
        r.setCreatedAt(now);

        assertEquals(id, r.getId());
        assertEquals(investmentId, r.getInvestmentId());
        assertEquals(TransactionType.BUY, r.getType());
        assertEquals(new BigDecimal("10"), r.getQuantity());
        assertEquals(new BigDecimal("100"), r.getPrice());
        assertEquals("USD", r.getCurrency());
        assertEquals(new BigDecimal("1.0"), r.getFxRateToHome());
        assertNotNull(r.getTxnDate());
        assertEquals(now, r.getCreatedAt());
    }

    @Test
    void instrumentSearchResult_GettersSetters() {
        InstrumentSearchResult r = new InstrumentSearchResult();
        r.setSymbol("MSFT");
        r.setName("Microsoft");
        r.setType("STOCK");
        r.setExchange("NASDAQ");
        r.setCurrency("USD");

        assertEquals("MSFT", r.getSymbol());
        assertEquals("Microsoft", r.getName());
        assertEquals("STOCK", r.getType());
        assertEquals("NASDAQ", r.getExchange());
        assertEquals("USD", r.getCurrency());
    }

    @Test
    void instrumentSearchResult_Constructor() {
        InstrumentSearchResult r = new InstrumentSearchResult("AAPL", "Apple", "STOCK", "NASDAQ", "USD");
        assertEquals("AAPL", r.getSymbol());
        assertEquals("Apple", r.getName());
    }

    @Test
    void dividendResponse_GettersSetters() {
        DividendResponse r = new DividendResponse();
        UUID id = UUID.randomUUID();
        UUID investmentId = UUID.randomUUID();
        Instant now = Instant.now();

        r.setId(id);
        r.setInvestmentId(investmentId);
        r.setAmount(new BigDecimal("10"));
        r.setDividendPerShare(new BigDecimal("0.5"));
        r.setCurrency("USD");
        r.setWithholdingTax(new BigDecimal("1.5"));
        r.setReinvestmentPrice(new BigDecimal("150"));
        r.setMode(DividendMode.DISTRIBUTIVE);
        r.setExDate(LocalDate.now());
        r.setPaymentDate(LocalDate.now());
        r.setCreatedAt(now);

        assertEquals(id, r.getId());
        assertEquals(investmentId, r.getInvestmentId());
        assertEquals(new BigDecimal("10"), r.getAmount());
        assertEquals(new BigDecimal("0.5"), r.getDividendPerShare());
        assertEquals("USD", r.getCurrency());
        assertEquals(new BigDecimal("1.5"), r.getWithholdingTax());
        assertEquals(new BigDecimal("150"), r.getReinvestmentPrice());
        assertEquals(DividendMode.DISTRIBUTIVE, r.getMode());
        assertNotNull(r.getExDate());
        assertNotNull(r.getPaymentDate());
        assertEquals(now, r.getCreatedAt());
    }

    @Test
    void investmentResponse_GettersSetters() {
        InvestmentResponse r = new InvestmentResponse();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        r.setId(id);
        r.setSymbol("AAPL");
        r.setName("Apple");
        r.setType(InvestmentType.STOCK);
        r.setCurrency("USD");
        r.setMetadata(Map.of("key", "val"));
        r.setCreatedAt(now);
        r.setUpdatedAt(now);

        assertEquals(id, r.getId());
        assertEquals("AAPL", r.getSymbol());
        assertEquals("Apple", r.getName());
        assertEquals(InvestmentType.STOCK, r.getType());
        assertEquals("USD", r.getCurrency());
        assertEquals("val", r.getMetadata().get("key"));
        assertEquals(now, r.getCreatedAt());
        assertEquals(now, r.getUpdatedAt());
    }

    @Test
    void investmentPnlResponse_GettersSetters() {
        InvestmentPnlResponse r = new InvestmentPnlResponse();
        r.setRealisedPnl(new BigDecimal("100"));
        r.setUnrealisedPnl(new BigDecimal("200"));
        r.setRealisedPnlLocal(new BigDecimal("90"));
        r.setUnrealisedPnlLocal(new BigDecimal("180"));
        r.setTotalCostBasis(new BigDecimal("1000"));
        r.setCurrentQuantity(new BigDecimal("10"));

        assertEquals(new BigDecimal("100"), r.getRealisedPnl());
        assertEquals(new BigDecimal("200"), r.getUnrealisedPnl());
        assertEquals(new BigDecimal("90"), r.getRealisedPnlLocal());
        assertEquals(new BigDecimal("180"), r.getUnrealisedPnlLocal());
        assertEquals(new BigDecimal("1000"), r.getTotalCostBasis());
        assertEquals(new BigDecimal("10"), r.getCurrentQuantity());
    }

    @Test
    void chatMessage_AllConstructors() {
        ChatMessage msg1 = new ChatMessage(Role.USER, "Hello");
        assertEquals(Role.USER, msg1.getRole());
        assertEquals("Hello", msg1.getContent());

        ToolCallRequest tcr = new ToolCallRequest("id1", "tool_name", "{}");
        ChatMessage msg2 = new ChatMessage(Role.MODEL, tcr);
        assertEquals(Role.MODEL, msg2.getRole());
        assertEquals(tcr, msg2.getToolCall());

        ChatMessage msg3 = new ChatMessage(Role.TOOL, "result", "call1", "my_tool");
        assertEquals(Role.TOOL, msg3.getRole());
        assertEquals("result", msg3.getContent());
        assertEquals("call1", msg3.getToolCallId());
        assertEquals("my_tool", msg3.getToolName());
    }

    @Test
    void chatMessage_SettersGetters() {
        ChatMessage msg = new ChatMessage(Role.USER, "test");
        msg.setRole(Role.MODEL);
        msg.setContent("new content");
        msg.setToolCallId("id123");
        msg.setToolName("tool");

        ToolCallRequest tcr = new ToolCallRequest();
        tcr.setId("tcr1");
        tcr.setName("name1");
        tcr.setArgumentsJson("{\"a\":1}");
        msg.setToolCall(tcr);

        assertEquals(Role.MODEL, msg.getRole());
        assertEquals("new content", msg.getContent());
        assertEquals("id123", msg.getToolCallId());
        assertEquals("tool", msg.getToolName());
        assertEquals("tcr1", msg.getToolCall().getId());
        assertEquals("name1", msg.getToolCall().getName());
        assertEquals("{\"a\":1}", msg.getToolCall().getArgumentsJson());
    }

    @Test
    void errorResponse_SettersAndTimestamp() {
        ErrorResponse er = new ErrorResponse("Err", "msg", 500);
        assertNotNull(er.getTimestamp());
        Instant ts = Instant.now();
        er.setTimestamp(ts);
        assertEquals(ts, er.getTimestamp());
    }

    @Test
    void model_PriceSnapshot_GettersSetters() {
        PriceSnapshot s = new PriceSnapshot();
        UUID id = UUID.randomUUID();
        UUID invId = UUID.randomUUID();
        Instant now = Instant.now();

        s.setId(id);
        s.setInvestmentId(invId);
        s.setPrice(new BigDecimal("150"));
        s.setCurrency("USD");
        s.setFetchedAt(now);

        assertEquals(id, s.getId());
        assertEquals(invId, s.getInvestmentId());
        assertEquals(new BigDecimal("150"), s.getPrice());
        assertEquals("USD", s.getCurrency());
        assertEquals(now, s.getFetchedAt());
    }

    @Test
    void model_FxRate_GettersSetters() {
        FxRate r = new FxRate();
        UUID id = UUID.randomUUID();
        r.setId(id);
        r.setFromCurrency("EUR");
        r.setToCurrency("USD");
        r.setRate(new BigDecimal("1.09"));
        r.setRateDate(LocalDate.now());

        assertEquals(id, r.getId());
        assertEquals("EUR", r.getFromCurrency());
        assertEquals("USD", r.getToCurrency());
        assertEquals(new BigDecimal("1.09"), r.getRate());
        assertNotNull(r.getRateDate());
    }

    @Test
    void model_Investment_GettersSetters() {
        Investment inv = new Investment();
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        inv.setId(id);
        inv.setSymbol("AAPL");
        inv.setName("Apple");
        inv.setType(InvestmentType.STOCK);
        inv.setCurrency("USD");
        inv.setMetadata(Map.of("sector", "tech"));
        inv.setCreatedAt(now);
        inv.setUpdatedAt(now);

        assertEquals(id, inv.getId());
        assertEquals("AAPL", inv.getSymbol());
        assertEquals("Apple", inv.getName());
        assertEquals(InvestmentType.STOCK, inv.getType());
        assertEquals("USD", inv.getCurrency());
        assertEquals("tech", inv.getMetadata().get("sector"));
        assertEquals(now, inv.getCreatedAt());
        assertEquals(now, inv.getUpdatedAt());
    }

    @Test
    void model_Transaction_GettersSetters() {
        Transaction t = new Transaction();
        UUID id = UUID.randomUUID();
        UUID invId = UUID.randomUUID();
        Instant now = Instant.now();

        t.setId(id);
        t.setInvestmentId(invId);
        t.setType(TransactionType.SELL);
        t.setQuantity(new BigDecimal("5"));
        t.setPrice(new BigDecimal("200"));
        t.setCurrency("EUR");
        t.setFxRateToHome(new BigDecimal("1.09"));
        t.setTxnDate(LocalDate.now());
        t.setCreatedAt(now);

        assertEquals(id, t.getId());
        assertEquals(invId, t.getInvestmentId());
        assertEquals(TransactionType.SELL, t.getType());
        assertEquals(new BigDecimal("5"), t.getQuantity());
        assertEquals(new BigDecimal("200"), t.getPrice());
        assertEquals("EUR", t.getCurrency());
        assertEquals(new BigDecimal("1.09"), t.getFxRateToHome());
        assertNotNull(t.getTxnDate());
        assertEquals(now, t.getCreatedAt());
    }
}
