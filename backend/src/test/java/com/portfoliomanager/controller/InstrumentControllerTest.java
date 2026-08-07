package com.portfoliomanager.controller;

import com.portfoliomanager.client.YahooFinanceSearchClient;
import com.portfoliomanager.dto.instrument.InstrumentSearchResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InstrumentControllerTest {

    @Mock private YahooFinanceSearchClient searchClient;

    private InstrumentController controller;

    @BeforeEach
    void setUp() {
        controller = new InstrumentController(searchClient);
    }

    @Test
    void search_DelegatesToSearchClient() {
        InstrumentSearchResult result1 = new InstrumentSearchResult("AAPL", "Apple Inc.", "STOCK", "NASDAQ", "USD");
        when(searchClient.search("AAPL")).thenReturn(List.of(result1));

        List<InstrumentSearchResult> results = controller.search("AAPL");

        assertEquals(1, results.size());
        assertEquals("AAPL", results.get(0).getSymbol());
        assertEquals("Apple Inc.", results.get(0).getName());
        assertEquals("STOCK", results.get(0).getType());
        assertEquals("NASDAQ", results.get(0).getExchange());
        assertEquals("USD", results.get(0).getCurrency());
    }

    @Test
    void search_TrimsInput() {
        when(searchClient.search("MSFT")).thenReturn(List.of());

        List<InstrumentSearchResult> results = controller.search("  MSFT  ");

        verify(searchClient).search("MSFT");
        assertTrue(results.isEmpty());
    }
}
