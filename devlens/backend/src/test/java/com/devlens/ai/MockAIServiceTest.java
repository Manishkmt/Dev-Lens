package com.devlens.ai;

import com.devlens.ai.service.MockAIService;
import com.devlens.ai.service.PromptTemplates;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MockAIServiceTest {

    private MockAIService mockAIService;

    @BeforeEach
    void setUp() {
        mockAIService = new MockAIService();
    }

    @Test
    @DisplayName("MockAIService responds with valid JSON for explanation")
    void shouldGenerateExplanationMock() {
        String systemPrompt = PromptTemplates.getExplainSystemPrompt("BEGINNER");
        String output = mockAIService.complete(systemPrompt, "int a = 1;");

        assertNotNull(output);
        assertTrue(output.contains("summary"));
        assertTrue(output.contains("stepByStep"));
        assertTrue(output.contains("conceptChips"));
    }

    @Test
    @DisplayName("MockAIService responds with valid JSON for debugging")
    void shouldGenerateDebugMock() {
        String systemPrompt = PromptTemplates.getDebugSystemPrompt("INTERMEDIATE");
        String output = mockAIService.complete(systemPrompt, "int a = 1;");

        assertNotNull(output);
        assertTrue(output.contains("issuesCount"));
        assertTrue(output.contains("suggestedFix"));
    }

    @Test
    @DisplayName("MockAIService responds with valid JSON for optimization")
    void shouldGenerateOptimizationMock() {
        String systemPrompt = PromptTemplates.getOptimizeSystemPrompt("ADVANCED");
        String output = mockAIService.complete(systemPrompt, "int a = 1;");

        assertNotNull(output);
        assertTrue(output.contains("jitStatus"));
        assertTrue(output.contains("tradeOffs"));
    }

    @Test
    @DisplayName("MockAIService responds with valid JSON for learning curriculum")
    void shouldGenerateLearningMock() {
        String systemPrompt = PromptTemplates.getLearningSystemPrompt("BEGINNER");
        String output = mockAIService.complete(systemPrompt, "Bubble Sort");

        assertNotNull(output);
        assertTrue(output.contains("concept"));
        assertTrue(output.contains("quiz"));
        assertTrue(output.contains("vivaQuestions"));
    }

    @Test
    @DisplayName("MockAIService responds with valid JSON for differential comparison")
    void shouldGenerateCompareMock() {
        String systemPrompt = PromptTemplates.getCompareSystemPrompt();
        String output = mockAIService.complete(systemPrompt, "codeA vs codeB");

        assertNotNull(output);
        assertTrue(output.contains("metrics"));
        assertTrue(output.contains("verdict"));
    }

    @Test
    @DisplayName("MockAIService responds with markdown text for chat assistant")
    void shouldGenerateChatMock() {
        String output = mockAIService.complete("You are DevLens AI Assistant", "Why does swap need a temporary variable?");

        assertNotNull(output);
        assertTrue(output.contains("temp") || output.contains("buffer") || output.contains("Java"));
    }
}
