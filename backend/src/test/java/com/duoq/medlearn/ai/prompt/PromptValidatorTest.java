package com.duoq.medlearn.ai.prompt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PromptValidatorTest {

    private PromptValidator validator;

    @BeforeEach
    void setUp() {
        validator = new PromptValidator();
    }

    @Test
    void validate_shouldReturnEmptyForValidInput() {
        assertThat(validator.validate("system prompt", "user prompt")).isEmpty();
    }

    @Test
    void validate_shouldReturnErrorsForEmptySystemPrompt() {
        var errors = validator.validate("", "user prompt");
        assertThat(errors).contains("System prompt must not be empty");
    }

    @Test
    void validate_shouldReturnErrorsForEmptyUserPrompt() {
        var errors = validator.validate("system", "");
        assertThat(errors).contains("User prompt template must not be empty");
    }

    @Test
    void validateOrThrow_shouldThrowForInvalidInput() {
        assertThatThrownBy(() -> validator.validateOrThrow("", ""))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiConfigurationException.class);
    }
}
