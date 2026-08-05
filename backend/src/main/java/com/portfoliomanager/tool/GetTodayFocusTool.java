package com.portfoliomanager.tool;

import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class GetTodayFocusTool implements PortfolioTool {

    @Override
    public String getName() {
        return "get_today_focus";
    }

    @Override
    public String getDescription() {
        return "Returns the main area or investment the user should focus on today, based on recent market movements.";
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
        return "The market is volatile today. Focus on reviewing your tech sector allocation, particularly MSFT.";
    }
}
