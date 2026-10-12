package com.devlens.ai;

import com.devlens.ai.dto.ExplainDto;
import com.devlens.ai.service.AiJsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class AiJsonParserTest {

    private AiJsonParser parser;

    @BeforeEach
    void setUp() {
        parser = new AiJsonParser(new ObjectMapper());
    }

    @Test
    @DisplayName("Parse valid pure JSON string into DTO")
    void shouldParseCleanJson() {
        String json = """
                {
                  "summary": "Sorts an array using bubble sort",
                  "confidence": "98.5%",
                  "signature": "public static void sort(int[] arr)"
                }
                """;

        Optional<ExplainDto.Response> res = parser.parse(json, ExplainDto.Response.class);
        assertTrue(res.isPresent());
        assertEquals("Sorts an array using bubble sort", res.get().getSummary());
        assertEquals("98.5%", res.get().getConfidence());
    }

    @Test
    @DisplayName("Parse JSON wrapped in markdown code blocks")
    void shouldStripMarkdownFences() {
        String wrappedJson = """
                Here is the analysis:
                ```json
                {
                  "summary": "Optimized sort with early termination",
                  "confidence": "99.0%"
                }
                ```
                Hope this helps!
                """;

        Optional<ExplainDto.Response> res = parser.parse(wrappedJson, ExplainDto.Response.class);
        assertTrue(res.isPresent());
        assertEquals("Optimized sort with early termination", res.get().getSummary());
    }

    @Test
    @DisplayName("Invalid or corrupted JSON returns empty Optional without crashing")
    void shouldSafelyHandleCorruptedJson() {
        String corrupted = "This is not JSON at all { malformed: 'test' ";
        Optional<ExplainDto.Response> res = parser.parse(corrupted, ExplainDto.Response.class);
        assertFalse(res.isPresent());
    }

    @Test
    @DisplayName("Null or empty string returns empty Optional safely")
    void shouldSafelyHandleNullOrEmpty() {
        assertFalse(parser.parse(null, ExplainDto.Response.class).isPresent());
        assertFalse(parser.parse("   ", ExplainDto.Response.class).isPresent());
    }
}
