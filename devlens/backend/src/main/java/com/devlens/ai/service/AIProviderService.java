package com.devlens.ai.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * Intelligent AI Provider Router
 * Selects between Gemini, OpenAI, and Mock implementations based on configuration
 * and environment variable AI_API_KEY. Provides zero-downtime fallback to MockAIService.
 */
@Slf4j
@Primary
@Service("aiProviderService")
public class AIProviderService implements AIService {

    private final AIService geminiAIService;
    private final AIService openAIAIService;
    private final AIService mockAIService;
    private final String provider;

    public AIProviderService(
            @Qualifier("geminiAIService") AIService geminiAIService,
            @Qualifier("openAIAIService") AIService openAIAIService,
            @Qualifier("mockAIService") AIService mockAIService,
            @Value("${devlens.ai.provider:gemini}") String provider) {
        this.geminiAIService = geminiAIService;
        this.openAIAIService = openAIAIService;
        this.mockAIService = mockAIService;
        this.provider = provider.trim().toLowerCase();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        String apiKey = System.getenv("AI_API_KEY");

        // 1. If no API key configured or provider explicitly set to 'mock', use MockAIService
        if (apiKey == null || apiKey.isBlank() || "mock".equals(provider)) {
            log.info("No AI_API_KEY found or mock mode selected. Using MockAIService for realistic offline responses.");
            return mockAIService.complete(systemPrompt, userPrompt);
        }

        // 2. Select live provider
        AIService selectedProvider = switch (provider) {
            case "openai" -> openAIAIService;
            default -> geminiAIService;
        };

        try {
            return selectedProvider.complete(systemPrompt, userPrompt);
        } catch (Exception e) {
            log.warn("Live AI provider ({}) failed: {}. Falling back seamlessly to MockAIService.",
                    provider, e.getMessage());
            return mockAIService.complete(systemPrompt, userPrompt);
        }
    }
}
