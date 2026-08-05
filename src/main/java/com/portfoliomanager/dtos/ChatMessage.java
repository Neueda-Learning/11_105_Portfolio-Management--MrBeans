package com.portfoliomanager.dtos;

public class ChatMessage {
    private Role role;
    private String content;

    // Only used when role == TOOL to satisfy OpenAI's requirement of tracking which tool we are answering
    private String toolCallId;
    private String toolName;

    // Only used when role == MODEL and it returns a tool call
    private ToolCallRequest toolCall;

    public ChatMessage(Role role, String content) {
        this.role = role;
        this.content = content;
    }

    public ChatMessage(Role role, ToolCallRequest toolCall) {
        this.role = role;
        this.toolCall = toolCall;
    }

    public ChatMessage(Role role, String content, String toolCallId, String toolName) {
        this.role = role;
        this.content = content;
        this.toolCallId = toolCallId;
        this.toolName = toolName;
    }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getToolCallId() { return toolCallId; }
    public void setToolCallId(String toolCallId) { this.toolCallId = toolCallId; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }

    public ToolCallRequest getToolCall() { return toolCall; }
    public void setToolCall(ToolCallRequest toolCall) { this.toolCall = toolCall; }
}
