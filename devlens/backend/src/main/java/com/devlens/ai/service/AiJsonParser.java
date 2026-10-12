package com.devlens.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Defensive AI JSON Parser
 * Strips markdown wrapping, cleans syntax anomalies, and extracts structured data safely.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiJsonParser {

    private final ObjectMapper objectMapper;

    /**
     * Extracts and parses a JSON string into a target class.
     * Returns Optional.empty() if invalid, without throwing exceptions.
     */
    public <T> Optional<T> parse(String rawText, Class<T> clazz) {
        if (rawText == null || rawText.isBlank()) {
            return Optional.empty();
        }

        try {
            String sanitized = cleanJsonText(rawText);
            T result = objectMapper.readValue(sanitized, clazz);
            return Optional.ofNullable(result);
        } catch (Exception e) {
            log.warn("Failed to parse AI output into {}: {}", clazz.getSimpleName(), e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Extracts raw JsonNode safely
     */
    public Optional<JsonNode> parseTree(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return Optional.empty();
        }

        try {
            String sanitized = cleanJsonText(rawText);
            return Optional.of(objectMapper.readTree(sanitized));
        } catch (Exception e) {
            log.warn("Failed to parse raw AI output as JsonNode: {}", e.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Cleans markdown fences, leading/trailing whitespace, and JSON prefix blocks
     */
    public String cleanJsonText(String text) {
        if (text == null) return "{}";
        String trimmed = text.trim();

        // Strip ```json and ```
        if (trimmed.startsWith("```json")) {
            trimmed = trimmed.substring(7);
        } else if (trimmed.startsWith("```")) {
            trimmed = trimmed.substring(3);
        }

        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3);
        }

        trimmed = trimmed.trim();

        // Extract substring between first '{' and last '}'
        int firstBrace = trimmed.indexOf('{');
        int lastBrace = trimmed.lastIndexOf('}');
        if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
            return trimmed.substring(firstBrace, lastBrace + 1);
        }

        // Or substring between first '[' and last ']'
        int firstBracket = trimmed.indexOf('[');
        int lastBracket = trimmed.lastIndexOf(']');
        if (firstBracket != -1 && lastBracket != -1 && lastBracket > firstBracket) {
            return trimmed.substring(firstBracket, lastBracket + 1);
        }

        return trimmed;
    }
}
