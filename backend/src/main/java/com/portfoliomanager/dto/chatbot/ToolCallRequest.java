package com.portfoliomanager.dto.chatbot;

public class ToolCallRequest {
    private String id;
    private String name;
    private String argumentsJson; // Keep it raw string so we can parse it locally

    public ToolCallRequest() {}

    public ToolCallRequest(String id, String name, String argumentsJson) {
        this.id = id;
        this.name = name;
        this.argumentsJson = argumentsJson;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getArgumentsJson() { return argumentsJson; }
    public void setArgumentsJson(String argumentsJson) { this.argumentsJson = argumentsJson; }
}
