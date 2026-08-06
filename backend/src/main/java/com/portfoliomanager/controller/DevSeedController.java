package com.portfoliomanager.controller;

import com.portfoliomanager.service.DevDataSeederService;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@Profile("local")
@RequestMapping("/api/dev")
public class DevSeedController {

    private final DevDataSeederService devDataSeederService;

    public DevSeedController(DevDataSeederService devDataSeederService) {
        this.devDataSeederService = devDataSeederService;
    }

    @PostMapping("/seed")
    public Map<String, Object> seed(
            @RequestParam(defaultValue = "18") int investments,
            @RequestParam(defaultValue = "36") int transactionsPerInvestment,
            @RequestParam(defaultValue = "180") int snapshotDays,
            @RequestParam(defaultValue = "true") boolean wipeExistingData
    ) {
        if (investments < 1 || investments > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "investments must be between 1 and 200");
        }
        if (transactionsPerInvestment < 2 || transactionsPerInvestment > 500) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "transactionsPerInvestment must be between 2 and 500");
        }
        if (snapshotDays < 30 || snapshotDays > 730) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "snapshotDays must be between 30 and 730");
        }

        DevDataSeederService.SeedSummary summary = devDataSeederService.seed(
                investments,
                transactionsPerInvestment,
                snapshotDays,
                wipeExistingData
        );

        return Map.of(
                "message", "Seed data generated successfully",
                "investments", summary.investments(),
                "transactions", summary.transactions(),
                "priceSnapshots", summary.priceSnapshots(),
                "wipeExistingData", wipeExistingData
        );
    }
}
