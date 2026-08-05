package com.portfoliomanager.client;

import com.portfoliomanager.dto.chatbot.ChatMessage;
import com.portfoliomanager.dto.chatbot.LlmResponse;
import com.portfoliomanager.dto.chatbot.ToolCallRequest;
import com.portfoliomanager.tool.PortfolioTool;

import java.util.List;

/**
 * Fake implementation used in tests so we don't hit live APIs.
 */
public class FakeLlmClient implements LlmClient {

    private boolean triggerToolNext = false;
    private String toolToTrigger = "";

    @Override
    public LlmResponse send(List<ChatMessage> history, List<PortfolioTool> tools) {
        if (triggerToolNext) {
            triggerToolNext = false; // reset
            return new LlmResponse(new ToolCallRequest("fake-call-id", toolToTrigger, "{}"));
        }
        
        return new LlmResponse("This is a canned response from the FakeLlmClient.");
    }

    public void setTriggerToolNext(String toolName) {
        this.triggerToolNext = true;
        this.toolToTrigger = toolName;
    }
}
