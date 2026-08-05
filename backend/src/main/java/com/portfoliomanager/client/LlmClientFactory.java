package com.portfoliomanager.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LlmClientFactory {

    @Value("${portfolio.chatbot.provider:groq}")
    private String provider;

    @Value("${portfolio.chatbot.groq.api-key:}")
    private String groqApiKey;

    @Value("${portfolio.chatbot.groq.model:}")
    private String groqModel;

    @Value("${portfolio.chatbot.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${portfolio.chatbot.gemini.model:}")
    private String geminiModel;

    @Bean
    public LlmClient activeLlmClient() {
        if ("fake".equalsIgnoreCase(provider)) {
            return new FakeLlmClient();
        } else if ("gemini".equalsIgnoreCase(provider)) {
            return new GeminiLlmClient(geminiApiKey, geminiModel);
        } else {
            // Default to groq
            return new GroqLlmClient(groqApiKey, groqModel);
        }
    }
}
