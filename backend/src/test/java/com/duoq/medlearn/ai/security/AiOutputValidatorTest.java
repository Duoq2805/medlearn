package com.duoq.medlearn.ai.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AiOutputValidatorTest {

    private AiOutputValidator validator;

    @BeforeEach
    void setUp() {
        validator = new AiOutputValidator();
    }

    @Test
    void validate_shouldPassNormalOutput() {
        assertThat(validator.validate("The treatment for diabetes includes insulin therapy."))
                .isEqualTo("The treatment for diabetes includes insulin therapy.");
    }

    @Test
    void validate_shouldFilterAsAnAiMessage() {
        assertThatThrownBy(() -> validator.validate("I am an AI language model designed to help"))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiOutputFilteredException.class);
    }

    @Test
    void validate_shouldFilterAsAnAiAssistant() {
        assertThatThrownBy(() -> validator.validate("As an AI assistant, I can help you with"))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiOutputFilteredException.class);
    }

    @Test
    void validate_shouldFilterLLMDisclaimer() {
        assertThatThrownBy(() -> validator.validate("As a large language model, I cannot"))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiOutputFilteredException.class);
    }

    @Test
    void validate_shouldHandleNull() {
        assertThat(validator.validate(null)).isEqualTo("");
    }
}
