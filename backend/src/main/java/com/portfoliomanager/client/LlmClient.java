package com.portfoliomanager.client;

import com.portfoliomanager.dto.chatbot.ChatMessage;
import com.portfoliomanager.dto.chatbot.LlmResponse;
import com.portfoliomanager.tool.PortfolioTool;

import java.util.List;

public interface LlmClient {
    /**
     * Sends a chat history and available tools to the LLM.
     * Returns either a text response or a tool invocation request.
     */
    LlmResponse send(List<ChatMessage> history, List<PortfolioTool> tools);
}
