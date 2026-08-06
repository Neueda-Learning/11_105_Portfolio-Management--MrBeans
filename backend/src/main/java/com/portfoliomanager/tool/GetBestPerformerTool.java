package com.portfoliomanager.tool;

import com.portfoliomanager.dto.dashboard.PerformanceResponse;
import com.portfoliomanager.service.DashboardService;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class GetBestPerformerTool implements PortfolioTool {

    private final DashboardService dashboardService;

    public GetBestPerformerTool(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public String getName() {
        return "get_best_performer";
    }

    @Override
    public String getDescription() {
        return "Returns the top performing investment in the portfolio based on total return percentage (including realised gains).";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        return Map.of(
            "type", "object",
            "properties", Map.of(
                "currency", Map.of(
                    "type", "string",
                    "description", "The home currency for conversion (e.g. USD, INR). Defaults to USD."
                )
            )
        );
    }

    @Override
    public Object execute(Map<String, Object> args) {
        String currency = args.getOrDefault("currency", "USD").toString();
        List<PerformanceResponse> performance = dashboardService.getPerformance(currency);

        return performance.stream()
                .filter(p -> p.getReturnPct() != null)
                .max(Comparator.comparing(PerformanceResponse::getReturnPct))
                .map(p -> Map.of(
                        "symbol", p.getSymbol(),
                        "name", p.getName() != null ? p.getName() : p.getSymbol(),
                        "returnPct", p.getReturnPct(),
                        "currentValue", p.getCurrentValue(),
                        "investmentType", p.getInvestmentType() != null ? p.getInvestmentType() : "UNKNOWN"
                ))
                .orElse(Map.of("message", "No investments with price data found"));
    }
}
