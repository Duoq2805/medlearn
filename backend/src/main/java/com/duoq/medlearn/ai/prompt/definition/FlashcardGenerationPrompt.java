package com.duoq.medlearn.ai.prompt.definition;

import org.springframework.stereotype.Component;

/**
 * In-code prompt for flashcard generation.
 * Variables: disease, count, difficulty, context
 */
@Component
public class FlashcardGenerationPrompt implements PromptDefinition {

    @Override
    public String getCode() {
        return "flashcard-gen";
    }

    @Override
    public String getSystemPrompt() {
        return """
                You are a medical education expert creating flashcards for medical students.
                Generate flashcards in Vietnamese. Use clear, precise medical language.
                Each flashcard must have a question and answer that tests understanding, not just recall.
                Return ONLY a JSON object with a 'flashcards' array.
                Each element must have: 'question', 'answer', 'explanation', 'source'.
                Do NOT include markdown code fences, markdown formatting, or any text outside the JSON.
                Example: {"flashcards":[{"question":"...","answer":"...","explanation":"...","source":"..."}]}""";
    }

    @Override
    public String getUserPromptTemplate() {
        return """
                Generate {{count}} flashcards about "{{disease}}" at {{difficulty}} difficulty level.

                Use the following source material:
                {{context}}

                Language: Vietnamese
                Return ONLY valid JSON as specified in the system prompt.""";
    }

    @Override
    public String getModel() {
        return "gpt-4o-mini";
    }

    @Override
    public double getTemperature() {
        return 0.3;
    }

    @Override
    public int getMaxTokens() {
        return 4000;
    }

    @Override
    public String getRequiredVariables() {
        return "disease,count,difficulty,context";
    }
}
