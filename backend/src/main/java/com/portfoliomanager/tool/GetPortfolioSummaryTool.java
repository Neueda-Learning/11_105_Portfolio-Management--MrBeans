package com.portfoliomanager.tool;

import com.portfoliomanager.dto.dashboard.DashboardSummaryResponse;
import com.portfoliomanager.service.DashboardService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class GetPortfolioSummaryTool implements PortfolioTool {

    private final DashboardService dashboardService;

    public GetPortfolioSummaryTool(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Override
    public String getName() {
        return "get_portfolio_summary";
    }

    @Override
    public String getDescription() {
        return "Returns the current portfolio summary including total value, cost basis, unrealised PnL, realised PnL, and dividend income for the year. Use this to answer questions about total portfolio value, gains, losses, or overall performance.";
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
        DashboardSummaryResponse summary = dashboardService.getSummary(currency);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalValue", summary.getTotalValue());
        result.put("totalCostBasis", summary.getTotalCostBasis());
        result.put("unrealisedPnl", summary.getTotalUnrealisedPnl());
        result.put("realisedPnl", summary.getTotalRealisedPnl());
        result.put("dividendIncomeThisYear", summary.getDividendIncomeThisYear());
        result.put("currency", currency);
        return result;
    }
}
