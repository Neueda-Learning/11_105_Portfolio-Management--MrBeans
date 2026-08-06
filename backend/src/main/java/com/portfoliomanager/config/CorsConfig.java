package com.portfoliomanager.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
            // Allow local and private-network dev origins across common ports.
            .allowedOriginPatterns(
                "http://localhost:*",
                "http://localhost",
                "http://127.0.0.1:*",
                "http://127.0.0.1",
                "http://10.*:*",
                "http://10.*",
                "http://172.*:*",
                "http://172.*",
                "http://192.168.*:*",
                "http://192.168.*",
                "https://10.*:*",
                "https://10.*",
                "https://172.*:*",
                "https://172.*",
                "https://192.168.*:*",
                "https://192.168.*"
            )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}