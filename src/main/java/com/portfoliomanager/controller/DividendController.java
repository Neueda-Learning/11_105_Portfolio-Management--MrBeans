package com.portfoliomanager.controller;

import com.portfoliomanager.services.DividendService;
import com.portfoliomanager.dtos.SimulateDividendRequest;
import com.portfoliomanager.dtos.SimulateDividendResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/investments/{investmentId}/dividends")
public class DividendController {

    private final DividendService dividendService;

    public DividendController(DividendService dividendService) {
        this.dividendService = dividendService;
    }

    @PostMapping("/simulate")
    public SimulateDividendResponse simulateDividend(
            @PathVariable UUID investmentId,
            @Valid @RequestBody SimulateDividendRequest request) {
        return dividendService.simulateDividend(investmentId, request);
    }
}
