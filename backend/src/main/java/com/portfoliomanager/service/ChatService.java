package com.portfoliomanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.portfoliomanager.client.LlmClient;
import com.portfoliomanager.dto.chatbot.ChatMessage;
import com.portfoliomanager.dto.chatbot.LlmResponse;
import com.portfoliomanager.dto.chatbot.Role;
import com.portfoliomanager.dto.chatbot.ToolCallRequest;
import com.portfoliomanager.tool.PortfolioTool;
import com.portfoliomanager.tool.ToolRegistry;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ChatService {

    private final LlmClient llmClient;
    private final ToolRegistry toolRegistry;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChatService(LlmClient llmClient, ToolRegistry toolRegistry) {
        // Notice we inject LlmClient interface directly, completely decoupled from Groq/Gemini logic
        this.llmClient = llmClient;
        this.toolRegistry = toolRegistry;
    }

    public String chat(String userMessage) {
        List<ChatMessage> history = new ArrayList<>();
        history.add(new ChatMessage(Role.SYSTEM, "You are a helpful portfolio management assistant. You can use tools to answer questions about the portfolio."));
        history.add(new ChatMessage(Role.USER, userMessage));

        int maxTurns = 5;
        for (int i = 0; i < maxTurns; i++) {
            LlmResponse response = llmClient.send(history, toolRegistry.getAllTools());

            if (!response.isToolCall()) {
                // Done
                return response.getTextContent();
            }

            // Handle tool call
            ToolCallRequest call = response.getToolCall();
            history.add(new ChatMessage(Role.MODEL, call));

            Optional<PortfolioTool> tool = toolRegistry.getTool(call.getName());
            String toolResultJson;
            if (tool.isPresent()) {
                try {
                    Map<String, Object> args = mapper.readValue(
                            call.getArgumentsJson().isEmpty() ? "{}" : call.getArgumentsJson(), 
                            Map.class
                    );
                    Object result = tool.get().execute(args);
                    toolResultJson = mapper.writeValueAsString(result);
                } catch (Exception e) {
                    toolResultJson = "{\"error\": \"Failed to execute tool or parse arguments\"}";
                }
            } else {
                toolResultJson = "{\"error\": \"Tool not found\"}";
            }

            history.add(new ChatMessage(Role.TOOL, toolResultJson, call.getId(), call.getName()));
        }

        return "I had to stop thinking because I used too many tools in a row. Please try asking again.";
    }
}
