package com.duoq.medlearn.ai.prompt;

import com.duoq.medlearn.ai.exception.AiConfigurationException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PromptValidator {

    public List<String> validate(String systemPrompt, String userPromptTemplate) {
        var errors = new ArrayList<String>();

        if (systemPrompt == null || systemPrompt.isBlank()) {
            errors.add("System prompt must not be empty");
        }
        if (userPromptTemplate == null || userPromptTemplate.isBlank()) {
            errors.add("User prompt template must not be empty");
        }
        if (systemPrompt != null && systemPrompt.length() > 100_000) {
            errors.add("System prompt exceeds maximum length of 100,000 characters");
        }
        if (userPromptTemplate != null && userPromptTemplate.length() > 100_000) {
            errors.add("User prompt template exceeds maximum length of 100,000 characters");
        }

        return errors;
    }

    public void validateOrThrow(String systemPrompt, String userPromptTemplate) {
        var errors = validate(systemPrompt, userPromptTemplate);
        if (!errors.isEmpty()) {
            throw new AiConfigurationException("Prompt validation failed: " + String.join("; ", errors));
        }
    }
}
