package com.portfoliomanager.tool;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GetTodayFocusToolTest {

    private final GetTodayFocusTool tool = new GetTodayFocusTool();

    @Test
    void getName_ReturnsCorrectName() {
        assertEquals("get_today_focus", tool.getName());
    }

    @Test
    void getDescription_ReturnsNonEmpty() {
        assertFalse(tool.getDescription().isEmpty());
    }

    @Test
    void getParameterSchema_ContainsType() {
        Map<String, Object> schema = tool.getParameterSchema();
        assertEquals("object", schema.get("type"));
    }

    @Test
    void execute_ReturnsStaticAdvice() {
        Object result = tool.execute(Map.of());
        assertNotNull(result);
        assertTrue(result.toString().contains("market"));
    }
}
