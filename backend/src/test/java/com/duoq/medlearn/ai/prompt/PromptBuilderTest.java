package com.duoq.medlearn.ai.prompt;

import com.duoq.medlearn.ai.enums.AiRole;
import com.duoq.medlearn.ai.exception.AiConfigurationException;
import com.duoq.medlearn.ai.prompt.entity.PromptTemplate;
import com.duoq.medlearn.ai.security.PromptInjectionFilter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PromptBuilderTest {

    @Mock
    private PromptTemplateService templateService;

    @Mock
    private PromptInjectionFilter injectionFilter;

    private PromptBuilder builder;

    @BeforeEach
    void setUp() {
        var renderer = new PromptRenderer();
        builder = new PromptBuilder(templateService, renderer, injectionFilter);
    }

    @Test
    void build_shouldReturnSystemAndUserMessages() {
        var template = PromptTemplate.builder()
                .code("test")
                .systemPrompt("You are a doctor")
                .userPromptTemplate("Diagnose {{symptom}}")
                .requiredVariables("symptom")
                .status("ACTIVE")
                .build();

        when(templateService.getActiveEntity("test")).thenReturn(template);
        when(injectionFilter.sanitizeVariables(anyMap())).thenAnswer(invocation -> invocation.getArgument(0));

        var messages = builder.build("test", Map.of("symptom", "headache"));

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getRole()).isEqualTo(AiRole.SYSTEM);
        assertThat(messages.get(0).getContent()).isEqualTo("You are a doctor");
        assertThat(messages.get(1).getRole()).isEqualTo(AiRole.USER);
        assertThat(messages.get(1).getContent()).isEqualTo("Diagnose headache");
    }

    @Test
    void build_shouldThrowWhenRequiredVariableMissing() {
        var template = PromptTemplate.builder()
                .code("test")
                .systemPrompt("sys")
                .userPromptTemplate("Hello {{name}}")
                .requiredVariables("name")
                .status("ACTIVE")
                .build();

        when(templateService.getActiveEntity("test")).thenReturn(template);

        assertThatThrownBy(() -> builder.build("test", Map.of()))
                .isInstanceOf(AiConfigurationException.class);
    }

    @Test
    void buildFromTemplate_shouldBuildMessagesDirectly() {
        var template = PromptTemplate.builder()
                .code("test")
                .systemPrompt("System")
                .userPromptTemplate("User {{x}}")
                .build();

        var messages = builder.buildFromTemplate(template, Map.of("x", "value"));

        assertThat(messages).hasSize(2);
        assertThat(messages.get(0).getContent()).isEqualTo("System");
        assertThat(messages.get(1).getContent()).isEqualTo("User value");
    }
}
