package com.portfoliomanager.client;

import com.portfoliomanager.dto.chatbot.ChatMessage;
import com.portfoliomanager.dto.chatbot.LlmResponse;
import com.portfoliomanager.dto.chatbot.Role;
import com.portfoliomanager.dto.chatbot.ToolCallRequest;
import com.portfoliomanager.tool.PortfolioTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GroqLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GroqLlmClient.class);
    
    private final RestClient restClient;
    private final String model;

    public GroqLlmClient(String apiKey, String model) {
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl("https://api.groq.com/openai/v1/chat/completions")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public LlmResponse send(List<ChatMessage> history, List<PortfolioTool> tools) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", this.model);
            requestBody.put("messages", history.stream().map(this::mapMessage).collect(Collectors.toList()));
            
            if (tools != null && !tools.isEmpty()) {
                requestBody.put("tools", tools.stream().map(this::mapTool).collect(Collectors.toList()));
                requestBody.put("tool_choice", "auto");
            }

            Map<String, Object> response = restClient.post()
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return parseResponse(response);
            
        } catch (Exception e) {
            log.error("Failed to call Groq LLM API", e);
            // Graceful degradation per Section 3.3
            return new LlmResponse("I am currently unable to reach my language models to process your request. Please try again later.");
        }
    }

    private Map<String, Object> mapMessage(ChatMessage msg) {
        Map<String, Object> openAiMsg = new HashMap<>();
        
        switch (msg.getRole()) {
            case SYSTEM: openAiMsg.put("role", "system"); break;
            case USER: openAiMsg.put("role", "user"); break;
            case MODEL: openAiMsg.put("role", "assistant"); break;
            case TOOL: openAiMsg.put("role", "tool"); break;
        }

        if (msg.getRole() == Role.TOOL) {
            openAiMsg.put("content", msg.getContent());
            openAiMsg.put("tool_call_id", msg.getToolCallId());
        } else if (msg.getRole() == Role.MODEL && msg.getToolCall() != null) {
            // Model requesting a tool
            openAiMsg.put("content", null);
            Map<String, Object> call = new HashMap<>();
            call.put("id", msg.getToolCall().getId());
            call.put("type", "function");
            
            Map<String, Object> function = new HashMap<>();
            function.put("name", msg.getToolCall().getName());
            function.put("arguments", msg.getToolCall().getArgumentsJson());
            call.put("function", function);
            
            openAiMsg.put("tool_calls", List.of(call));
        } else {
            openAiMsg.put("content", msg.getContent());
        }
        
        return openAiMsg;
    }

    private Map<String, Object> mapTool(PortfolioTool tool) {
        Map<String, Object> function = new HashMap<>();
        function.put("name", tool.getName());
        function.put("description", tool.getDescription());
        function.put("parameters", tool.getParameterSchema());

        Map<String, Object> openAiTool = new HashMap<>();
        openAiTool.put("type", "function");
        openAiTool.put("function", function);
        return openAiTool;
    }

    private LlmResponse parseResponse(Map<String, Object> response) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
        if (choices == null || choices.isEmpty()) {
            return new LlmResponse("No response generated.");
        }
        
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        
        List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) message.get("tool_calls");
        if (toolCalls != null && !toolCalls.isEmpty()) {
            Map<String, Object> toolCall = toolCalls.get(0);
            String id = (String) toolCall.get("id");
            Map<String, Object> function = (Map<String, Object>) toolCall.get("function");
            String name = (String) function.get("name");
            String args = (String) function.get("arguments");
            
            return new LlmResponse(new ToolCallRequest(id, name, args));
        }
        
        return new LlmResponse((String) message.get("content"));
    }
}
