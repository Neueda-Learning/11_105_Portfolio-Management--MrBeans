package com.portfoliomanager.tool;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ToolRegistryTest {

    @Test
    void getAllTools_ReturnsAllRegistered() {
        PortfolioTool tool1 = mock(PortfolioTool.class);
        when(tool1.getName()).thenReturn("tool_a");
        PortfolioTool tool2 = mock(PortfolioTool.class);
        when(tool2.getName()).thenReturn("tool_b");

        ToolRegistry registry = new ToolRegistry(List.of(tool1, tool2));

        List<PortfolioTool> all = registry.getAllTools();
        assertEquals(2, all.size());
    }

    @Test
    void getTool_ExistingName_ReturnsPresent() {
        PortfolioTool tool = mock(PortfolioTool.class);
        when(tool.getName()).thenReturn("test_tool");

        ToolRegistry registry = new ToolRegistry(List.of(tool));

        Optional<PortfolioTool> result = registry.getTool("test_tool");
        assertTrue(result.isPresent());
        assertEquals("test_tool", result.get().getName());
    }

    @Test
    void getTool_MissingName_ReturnsEmpty() {
        ToolRegistry registry = new ToolRegistry(List.of());

        Optional<PortfolioTool> result = registry.getTool("nonexistent");
        assertTrue(result.isEmpty());
    }
}
