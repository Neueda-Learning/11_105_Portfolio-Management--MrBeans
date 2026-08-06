package com.portfoliomanager.controller;

import com.portfoliomanager.service.DividendService;

import com.portfoliomanager.dto.dividend.CreateDividendRequest;
import com.portfoliomanager.dto.dividend.DividendResponse;
import com.portfoliomanager.dto.dividend.SimulateDividendRequest;
import com.portfoliomanager.dto.dividend.SimulateDividendResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/investments/{investmentId}/dividends")
public class DividendController {

    private final DividendService dividendService;

    public DividendController(DividendService dividendService) {
        this.dividendService = dividendService;
    }

    @GetMapping
    public List<DividendResponse> getDividendsByInvestment(@PathVariable UUID investmentId) {
        return dividendService.getDividendsByInvestment(investmentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DividendResponse createDividend(
            @PathVariable UUID investmentId,
            @Valid @RequestBody CreateDividendRequest request) {
        return dividendService.createDividend(investmentId, request);
    }

    @DeleteMapping("/{dividendId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDividend(
            @PathVariable UUID investmentId,
            @PathVariable UUID dividendId) {
        dividendService.deleteDividend(investmentId, dividendId);
    }

    @PostMapping("/simulate")
    public SimulateDividendResponse simulateDividend(
            @PathVariable UUID investmentId,
            @Valid @RequestBody SimulateDividendRequest request) {
        return dividendService.simulateDividend(investmentId, request);
    }
}
