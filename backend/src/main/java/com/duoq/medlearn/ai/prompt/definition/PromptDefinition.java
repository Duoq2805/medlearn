package com.duoq.medlearn.ai.prompt.definition;

/**
 * In-code prompt definition for AI generation features.
 * Use this for application-owned prompts (Flashcard, Quiz, etc.)
 * that are version-controlled in source code and do not require
 * runtime admin management.
 *
 * For admin-managed prompts (e.g. disease-summary), continue using
 * PromptTemplate loaded from DB via PromptTemplateService.
 */
public interface PromptDefinition {

    /** Unique identifier matching the feature (used for logging/audit only, not DB lookup). */
    String getCode();

    String getSystemPrompt();

    String getUserPromptTemplate();

    /** Default model ID. May be overridden by caller. */
    String getModel();

    /** Default temperature. May be overridden by caller. */
    double getTemperature();

    /** Default max output tokens. May be overridden by caller. */
    int getMaxTokens();

    /**
     * Comma-separated required variable names.
     * Validated by PromptBuilder before rendering.
     */
    String getRequiredVariables();
}
