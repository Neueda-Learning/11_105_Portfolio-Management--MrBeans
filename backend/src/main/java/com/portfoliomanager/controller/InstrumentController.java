package com.portfoliomanager.controller;

import com.portfoliomanager.client.YahooFinanceSearchClient;
import com.portfoliomanager.dto.instrument.InstrumentSearchResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/instruments")
public class InstrumentController {

    private final YahooFinanceSearchClient searchClient;

    public InstrumentController(YahooFinanceSearchClient searchClient) {
        this.searchClient = searchClient;
    }

    @GetMapping("/search")
    public List<InstrumentSearchResult> search(
            @RequestParam
            @NotBlank(message = "q must not be blank")
            @Size(max = 80, message = "q must be 80 characters or fewer")
            String q) {
        return searchClient.search(q.trim());
    }
}
