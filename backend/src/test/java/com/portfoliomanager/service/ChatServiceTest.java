package com.portfoliomanager.service;

import com.portfoliomanager.client.FakeLlmClient;
import com.portfoliomanager.tool.ToolRegistry;
import com.portfoliomanager.tool.GetBestPerformerTool;
import com.portfoliomanager.tool.GetTodayFocusTool;
import com.portfoliomanager.client.LlmClient;
import com.portfoliomanager.service.DashboardService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ChatServiceTest {

    private FakeLlmClient fakeLlmClient;
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        fakeLlmClient = new FakeLlmClient();
        DashboardService mockDashboard = mock(DashboardService.class);
        
        ToolRegistry registry = new ToolRegistry(List.of(
                new GetBestPerformerTool(mockDashboard), 
                new GetTodayFocusTool()
        ));
        
        chatService = new ChatService(fakeLlmClient, registry);
    }

    @Test
    void chat_ReturnsTextImmediately_IfNoToolRequested() {
        String response = chatService.chat("Hello there");
        
        // FakeLlmClient defaults to not triggering tools, thus returning the standard canned text
        assertEquals("This is a canned response from the FakeLlmClient.", response);
    }

    @Test
    void chat_ExecutesToolAndReturnsResult() {
        // Force the fake client to request a tool on its first turn
        fakeLlmClient.setTriggerToolNext("get_today_focus");

        String response = chatService.chat("What should I focus on?");
        
        // Loop will execute:
        // 1. Send text -> Receives ToolCall
        // 2. Looks up GetTodayFocusTool -> Executed locally
        // 3. Serializes output and resends to LlmClient
        // 4. LlmClient (having reset the flag) responds with standard text.
        assertEquals("This is a canned response from the FakeLlmClient.", response);
    }
}
