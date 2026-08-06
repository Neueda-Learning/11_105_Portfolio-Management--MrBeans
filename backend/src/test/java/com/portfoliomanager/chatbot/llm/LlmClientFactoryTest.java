package com.portfoliomanager.chatbot.llm;

import com.portfoliomanager.client.LlmClientFactory;
import com.portfoliomanager.client.LlmClient;
import com.portfoliomanager.client.GroqLlmClient;
import com.portfoliomanager.client.GeminiLlmClient;
import com.portfoliomanager.client.FakeLlmClient;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

class LlmClientFactoryTest {

    @Test
    void activeLlmClient_ReturnsGroqByDefault() {
        LlmClientFactory factory = new LlmClientFactory();
        ReflectionTestUtils.setField(factory, "provider", "groq");
        
        LlmClient client = factory.activeLlmClient();
        assertTrue(client instanceof GroqLlmClient);
    }

    @Test
    void activeLlmClient_ReturnsGeminiWhenConfigured() {
        LlmClientFactory factory = new LlmClientFactory();
        ReflectionTestUtils.setField(factory, "provider", "gemini");
        
        LlmClient client = factory.activeLlmClient();
        assertTrue(client instanceof GeminiLlmClient);
    }

    @Test
    void activeLlmClient_ReturnsFakeWhenConfigured() {
        LlmClientFactory factory = new LlmClientFactory();
        ReflectionTestUtils.setField(factory, "provider", "fake");
        
        LlmClient client = factory.activeLlmClient();
        assertTrue(client instanceof FakeLlmClient);
    }
}
