package com.devlens.ai.service;

/**
 * Common AI Service Interface
 * Abstracts LLM interactions across Gemini, OpenAI, and Mock providers.
 */
public interface AIService {

    /**
     * Executes a completion request given system instructions and user prompt.
     *
     * @param systemPrompt Instructions, context, and output schema guidance
     * @param userPrompt Code snippets, user query, or topic
     * @return Raw completion string (usually JSON or markdown text)
     */
    String complete(String systemPrompt, String userPrompt);
}
