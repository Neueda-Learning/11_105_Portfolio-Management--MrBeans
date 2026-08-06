package com.portfoliomanager.controller;

import com.portfoliomanager.client.YahooFinanceSearchClient;
import com.portfoliomanager.dto.instrument.InstrumentSearchResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final YahooFinanceSearchClient searchClient;

    public InstrumentController(YahooFinanceSearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @GetMapping("/search")
    public List<InstrumentSearchResult> search(@RequestParam String q) {
        if (q == null || q.isBlank() || q.length() < 1) return List.of();
        return searchClient.search(q.trim());
    }
}
