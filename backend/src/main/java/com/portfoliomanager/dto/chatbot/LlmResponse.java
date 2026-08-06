package com.portfoliomanager.dto.chatbot;

public class LlmResponse {
    private String textContent;
    private ToolCallRequest toolCall;

    public LlmResponse(String textContent) {
        this.textContent = textContent;
    }

    public LlmResponse(ToolCallRequest toolCall) {
        this.toolCall = toolCall;
    }

    public boolean isToolCall() {
        return toolCall != null;
    }

    public String getTextContent() { return textContent; }
    public ToolCallRequest getToolCall() { return toolCall; }
}
