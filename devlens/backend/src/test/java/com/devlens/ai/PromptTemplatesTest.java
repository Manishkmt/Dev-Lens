package com.devlens.ai;

import com.devlens.ai.service.PromptTemplates;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PromptTemplatesTest {

    @Test
    @DisplayName("Explain system prompts adapt depth based on pedagogical level")
    void shouldAdaptExplainPromptByLevel() {
        String beginnerPrompt = PromptTemplates.getExplainSystemPrompt("BEGINNER");
        String advancedPrompt = PromptTemplates.getExplainSystemPrompt("ADVANCED");
        String intermediatePrompt = PromptTemplates.getExplainSystemPrompt("INTERMEDIATE");

        assertTrue(beginnerPrompt.contains("novice") || beginnerPrompt.contains("CS1"));
        assertTrue(advancedPrompt.contains("senior") || advancedPrompt.contains("JIT"));
        assertTrue(intermediatePrompt.contains("intermediate"));

        assertTrue(beginnerPrompt.contains("JSON"));
        assertTrue(beginnerPrompt.contains("lineByLine"));
        assertTrue(beginnerPrompt.contains("conceptChips"));
    }

    @Test
    @DisplayName("Debug prompt contains issue schema instructions")
    void shouldBuildDebugPrompt() {
        String prompt = PromptTemplates.getDebugSystemPrompt("INTERMEDIATE");
        assertTrue(prompt.contains("issuesCount"));
        assertTrue(prompt.contains("suggestedFix"));
        assertTrue(prompt.contains("beforeAfterDiff"));
    }

    @Test
    @DisplayName("Optimize prompt contains trade-off instructions")
    void shouldBuildOptimizePrompt() {
        String prompt = PromptTemplates.getOptimizeSystemPrompt("ADVANCED");
        assertTrue(prompt.contains("jitStatus"));
        assertTrue(prompt.contains("tradeOffs"));
        assertTrue(prompt.contains("benchmark"));
    }

    @Test
    @DisplayName("Docs prompt contains Javadoc instructions")
    void shouldBuildDocsPrompt() {
        String prompt = PromptTemplates.getDocsSystemPrompt("JAVADOC");
        assertTrue(prompt.contains("javadoc"));
        assertTrue(prompt.contains("markdown"));
        assertTrue(prompt.contains("projectSummary"));
    }

    @Test
    @DisplayName("Learning prompt includes quiz and viva questions")
    void shouldBuildLearningPrompt() {
        String prompt = PromptTemplates.getLearningSystemPrompt("BEGINNER");
        assertTrue(prompt.contains("concept"));
        assertTrue(prompt.contains("quiz"));
        assertTrue(prompt.contains("vivaQuestions"));
    }

    @Test
    @DisplayName("Compare prompt includes metrics and verdict schema")
    void shouldBuildComparePrompt() {
        String prompt = PromptTemplates.getCompareSystemPrompt();
        assertTrue(prompt.contains("metrics"));
        assertTrue(prompt.contains("verdict"));
        assertTrue(prompt.contains("variantA"));
    }

    @Test
    @DisplayName("Chat prompt injects optional code and analysis context")
    void shouldBuildChatPromptWithContext() {
        String code = "int a = 10;";
        String analysis = "O(1) time complexity";
        String promptWithContext = PromptTemplates.getChatSystemPrompt(code, analysis);

        assertTrue(promptWithContext.contains("int a = 10;"));
        assertTrue(promptWithContext.contains("O(1) time complexity"));

        String promptNoContext = PromptTemplates.getChatSystemPrompt(null, null);
        assertFalse(promptNoContext.contains("CURRENT USER CODE IN EDITOR"));
    }
}
