package com.devlens.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real OpenAI Service implementation using Spring 6 RestClient.
 * Interacts with OpenAI Chat Completions REST API using the configured model.
 */
@Slf4j
@Service("openAIAIService")
public class OpenAIAIService implements AIService {

    private final RestClient restClient;
    private final String model;
    private final ObjectMapper objectMapper;

    public OpenAIAIService(
            @Value("${devlens.ai.openai.model:gpt-4o-mini}") String model,
            @Value("${devlens.ai.openai.base-url:https://api.openai.com/v1}") String baseUrl,
            @Value("${devlens.ai.timeout-ms:15000}") int timeoutMs,
            ObjectMapper objectMapper) {
        this.model = model;
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
            throw new IllegalStateException("AI_API_KEY environment variable is not configured for OpenAIAIService.");
        }

        log.info("Dispatching live AI request to OpenAI (model: {})", model);

        List<Map<String, String>> messages = new ArrayList<>();
        if (systemPrompt != null && !systemPrompt.isBlank()) {
            messages.add(Map.of("role", "system", "content", systemPrompt));
        }
        messages.add(Map.of("role", "user", "content", userPrompt != null ? userPrompt : ""));

        Map<String, Object> requestPayload = new HashMap<>();
        requestPayload.put("model", model);
        requestPayload.put("messages", messages);
        requestPayload.put("temperature", 0.2);

        if (systemPrompt != null && systemPrompt.contains("JSON")) {
            requestPayload.put("response_format", Map.of("type", "json_object"));
        }

        try {
            String responseBody = restClient.post()
                    .uri("/chat/completions")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestPayload)
                    .retrieve()
                    .body(String.class);

            if (responseBody == null || responseBody.isBlank()) {
                throw new RuntimeException("Empty response received from OpenAI API.");
            }

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode textNode = root.path("choices").path(0).path("message").path("content");

            if (textNode.isMissingNode()) {
                throw new RuntimeException("Malformed OpenAI API response: no choice text found.");
            }

            return textNode.asText();

        } catch (Exception e) {
            log.error("OpenAI API request failed: {}", e.getMessage());
            throw new RuntimeException("OpenAI completion error: " + e.getMessage(), e);
        }
    }
}
