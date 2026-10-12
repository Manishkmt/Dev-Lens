package com.devlens.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Real Google Gemini AI Service implementation using Spring 6 RestClient.
 * Interacts with Google Generative Language REST API using the configured model.
 */
@Slf4j
@Service("geminiAIService")
public class GeminiAIService implements AIService {

    private final RestClient restClient;
    private final String model;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public GeminiAIService(
            @Value("${devlens.ai.gemini.model:gemini-1.5-flash}") String model,
            @Value("${devlens.ai.gemini.base-url:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${devlens.ai.timeout-ms:15000}") int timeoutMs,
            ObjectMapper objectMapper) {
        this.model = model;
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(timeoutMs));
        factory.setReadTimeout(Duration.ofMillis(timeoutMs));

        this.restClient = RestClient.builder()
                .requestFactory(factory)
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public String complete(String systemPrompt, String userPrompt) {
        String apiKey = System.getenv("AI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("AI_API_KEY environment variable is not configured for GeminiAIService.");
        }

        log.info("Dispatching live AI request to Google Gemini (model: {})", model);

        String combinedPrompt = (systemPrompt != null && !systemPrompt.isBlank())
                ? systemPrompt + "\n\n" + userPrompt
                : userPrompt;

        Map<String, Object> requestPayload = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text", combinedPrompt))
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "responseMimeType", systemPrompt != null && systemPrompt.contains("JSON") ? "application/json" : "text/plain"
                )
        );

        try {
            String responseBody = restClient.post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/models/" + model + ":generateContent")
                            .queryParam("key", apiKey)
                            .build())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new RuntimeException("Empty response received from Gemini API.");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");

            if (textNode.isMissingNode()) {
                throw new RuntimeException("Malformed Gemini API response: no candidate text found.");
            }

            return textNode.asText();

        } catch (Exception e) {
            log.error("Gemini API request failed: {}", e.getMessage());
            throw new RuntimeException("Gemini completion error: " + e.getMessage(), e);
        }
    }
}
