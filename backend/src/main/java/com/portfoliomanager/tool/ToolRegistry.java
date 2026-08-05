package com.portfoliomanager.tool;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class ToolRegistry {

    private final Map<String, PortfolioTool> tools;

    public ToolRegistry(List<PortfolioTool> toolList) {
        // Automatically wire all beans implementing PortfolioTool into a lookup map
        this.tools = toolList.stream()
                .collect(Collectors.toMap(PortfolioTool::getName, Function.identity()));
    }

    public List<PortfolioTool> getAllTools() {
        return List.copyOf(tools.values());
    }

    public Optional<PortfolioTool> getTool(String name) {
        return Optional.ofNullable(tools.get(name));
    }
}
