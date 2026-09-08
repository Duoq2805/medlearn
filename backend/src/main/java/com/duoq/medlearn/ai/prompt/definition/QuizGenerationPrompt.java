package com.duoq.medlearn.ai.prompt.definition;

import org.springframework.stereotype.Component;

/**
 * In-code prompt for quiz generation.
 * Variables: topic, count, context
 */
@Component
public class QuizGenerationPrompt implements PromptDefinition {

    @Override
    public String getCode() {
        return "quiz-gen";
    }

    @Override
    public String getSystemPrompt() {
        return """
                You are a medical education expert creating multiple-choice quiz questions for medical students.
                Generate questions in Vietnamese. Use clear, precise medical language.
                Each question must have exactly 4 options (A, B, C, D) and one correct answer.
                Return ONLY a JSON object with a 'questions' array.
                Each element must have: 'content', 'optionA', 'optionB', 'optionC', 'optionD', 'correctAnswer' (A/B/C/D), 'explanation'.
                Do NOT include markdown code fences or any text outside the JSON.
                Example: {"questions":[{"content":"...","optionA":"...","optionB":"...","optionC":"...","optionD":"...","correctAnswer":"A","explanation":"..."}]}""";
    }

    @Override
    public String getUserPromptTemplate() {
        return """
                Generate {{count}} multiple-choice questions about "{{topic}}".

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
        return 6000;
    }

    @Override
    public String getRequiredVariables() {
        return "topic,count,context";
    }
}
