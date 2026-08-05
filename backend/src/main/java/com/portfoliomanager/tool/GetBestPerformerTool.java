package com.portfoliomanager.tool;

import com.portfoliomanager.service.DashboardService;
import com.portfoliomanager.repository.InvestmentRepository;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetBestPerformerTool implements PortfolioTool {

    @Override
    public String getName() {
        return "get_best_performer";
    }

    @Override
    public String getDescription() {
        return "Returns the top performing investment in the portfolio based on unrealised PNL.";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of()
        );
    }

    @Override
    public Object execute(Map<String, Object> args) {
        // Stub for MVP. In reality, would query DashboardService or InvestmentRepository
        return Map.of(
            "symbol", "AAPL",
            "unrealisedPnl", 1250.00
        );
    }
}
