package com.portfoliomanager.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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

public class GeminiLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiLlmClient.class);
    
    private final RestClient restClient;
    private final String url;
    private final ObjectMapper mapper = new ObjectMapper();

    public GeminiLlmClient(String apiKey, String model) {
        // e.g. https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=...
        this.url = "https://generativelanguage.googleapis.com/v1beta/models/" + model + ":generateContent?key=" + apiKey;
        this.restClient = RestClient.builder()
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public LlmResponse send(List<ChatMessage> history, List<PortfolioTool> tools) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            
            // Extract SYSTEM messages to map to Gemini's systemInstruction
            String systemText = history.stream()
                .filter(m -> m.getRole() == Role.SYSTEM)
                .map(ChatMessage::getContent)
                .collect(Collectors.joining("\n"));
                
            if (!systemText.isEmpty()) {
                requestBody.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemText))));
            }

            // Map all non-SYSTEM messages
            List<Map<String, Object>> contents = history.stream()
                .filter(m -> m.getRole() != Role.SYSTEM)
                .map(this::mapMessage)
                .collect(Collectors.toList());
            requestBody.put("contents", contents);

            if (tools != null && !tools.isEmpty()) {
                Map<String, Object> toolDecl = new HashMap<>();
                toolDecl.put("functionDeclarations", tools.stream().map(this::mapTool).collect(Collectors.toList()));
                requestBody.put("tools", List.of(toolDecl));
            }

            Map<String, Object> response = restClient.post()
                    .uri(url)
                    .body(requestBody)
                    .retrieve()
                    .body(Map.class);

            return parseResponse(response);
            
        } catch (Exception e) {
            log.error("Failed to call Gemini LLM API", e);
            return new LlmResponse("I am currently unable to reach my language models to process your request. Please try again later.");
        }
    }

    private Map<String, Object> mapMessage(ChatMessage msg) {
        Map<String, Object> geminiMsg = new HashMap<>();
        
        if (msg.getRole() == Role.USER) {
            geminiMsg.put("role", "user");
            geminiMsg.put("parts", List.of(Map.of("text", msg.getContent())));
        } else if (msg.getRole() == Role.MODEL && msg.getToolCall() == null) {
            geminiMsg.put("role", "model");
            geminiMsg.put("parts", List.of(Map.of("text", msg.getContent())));
        } else if (msg.getRole() == Role.MODEL && msg.getToolCall() != null) {
            geminiMsg.put("role", "model");
            Map<String, Object> call = new HashMap<>();
            call.put("name", msg.getToolCall().getName());
            
            Map<String, Object> args = new HashMap<>();
            try {
                args = mapper.readValue(msg.getToolCall().getArgumentsJson(), Map.class);
            } catch (JsonProcessingException e) {
                // Ignore parse errors on args during mapping history
            }
            call.put("args", args);
            geminiMsg.put("parts", List.of(Map.of("functionCall", call)));
            
        } else if (msg.getRole() == Role.TOOL) {
            geminiMsg.put("role", "user"); // Gemini expects functionResponse from the user
            Map<String, Object> functionResponse = new HashMap<>();
            functionResponse.put("name", msg.getToolName());
            
            Map<String, Object> responseJson = new HashMap<>();
            try {
                // Attempt to parse tool return as JSON object. If it fails, wrap in result.
                Object parsed = mapper.readValue(msg.getContent(), Object.class);
                if (parsed instanceof Map) {
                    responseJson = (Map<String, Object>) parsed;
                } else {
                    responseJson.put("result", parsed);
                }
            } catch (JsonProcessingException e) {
                responseJson.put("result", msg.getContent());
            }
            
            functionResponse.put("response", responseJson);
            geminiMsg.put("parts", List.of(Map.of("functionResponse", functionResponse)));
        }
        
        return geminiMsg;
    }

    private Map<String, Object> mapTool(PortfolioTool tool) {
        Map<String, Object> function = new HashMap<>();
        function.put("name", tool.getName());
        function.put("description", tool.getDescription());
        
        // Convert to Gemini JSON Schema format if needed, but standard schema is mostly compatible
        Map<String, Object> schema = new HashMap<>(tool.getParameterSchema());
        // Gemini strictly wants 'type' to be uppercase, but often accepts standard schema too. 
        // We will pass it exactly as defined in the tool.
        function.put("parameters", schema);
        return function;
    }

    private LlmResponse parseResponse(Map<String, Object> response) {
        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        if (candidates == null || candidates.isEmpty()) {
            return new LlmResponse("No response generated.");
        }
        
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        if (content == null) return new LlmResponse("Empty content.");
        
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        if (parts != null && !parts.isEmpty()) {
            Map<String, Object> part = parts.get(0);
            if (part.containsKey("functionCall")) {
                Map<String, Object> functionCall = (Map<String, Object>) part.get("functionCall");
                String name = (String) functionCall.get("name");
                Object args = functionCall.get("args");
                String argsJson = "{}";
                try {
                    if (args != null) argsJson = mapper.writeValueAsString(args);
                } catch (JsonProcessingException e) {
                    // ignore
                }
                // Gemini doesn't use tool call IDs in the same way, generating a random one
                return new LlmResponse(new ToolCallRequest("call_" + System.currentTimeMillis(), name, argsJson));
            } else if (part.containsKey("text")) {
                return new LlmResponse((String) part.get("text"));
            }
        }
        
        return new LlmResponse("Unrecognized response format.");
    }
}
