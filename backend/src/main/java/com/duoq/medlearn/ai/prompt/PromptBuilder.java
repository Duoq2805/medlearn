package com.duoq.medlearn.ai.prompt;

import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.enums.AiRole;
import com.duoq.medlearn.ai.exception.AiConfigurationException;
import com.duoq.medlearn.ai.prompt.definition.PromptDefinition;
import com.duoq.medlearn.ai.prompt.entity.PromptTemplate;
import com.duoq.medlearn.ai.security.PromptInjectionFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptBuilder {

    private final PromptTemplateService promptTemplateService;
    private final PromptRenderer promptRenderer;
    private final PromptInjectionFilter injectionFilter;

    public List<AiMessage> build(String promptCode, Map<String, String> variables) {
        return build(promptCode, null, variables);
    }

    public List<AiMessage> build(String promptCode, String version, Map<String, String> variables) {
        var template = version != null
                ? promptTemplateService.getEntity(promptCode, version)
                : promptTemplateService.getActiveEntity(promptCode);

        validateRequiredVariables(template, variables);

        var sanitized = injectionFilter.sanitizeVariables(variables);
        return buildMessages(template, sanitized);
    }

    public List<AiMessage> buildFromTemplate(PromptTemplate template, Map<String, String> variables) {
        validateRequiredVariables(template, variables);
        return buildMessages(template, variables);
    }

    /**
     * Build messages from an in-code {@link PromptDefinition} (no DB lookup).
     * Applies injection filter on dynamic variables before rendering.
     * Use this for application-owned prompts (Flashcard, Quiz, etc.).
     */
    public List<AiMessage> buildFromDefinition(PromptDefinition definition, Map<String, String> variables) {
        validateRequiredVariables(definition, variables);
        var sanitized = injectionFilter.sanitizeVariables(variables);

        var messages = new ArrayList<AiMessage>();
        var system = definition.getSystemPrompt();
        if (system != null && !system.isBlank()) {
            messages.add(AiMessage.builder().role(AiRole.SYSTEM).content(system).build());
        }
        var rendered = promptRenderer.render(definition.getUserPromptTemplate(), sanitized);
        messages.add(AiMessage.builder().role(AiRole.USER).content(rendered).build());
        return messages;
    }

    private void validateRequiredVariables(PromptDefinition definition, Map<String, String> variables) {
        var required = definition.getRequiredVariables();
        if (required == null || required.isBlank()) return;
        for (var varName : required.split(",")) {
            var trimmed = varName.trim();
            if (!trimmed.isEmpty() && (variables == null || !variables.containsKey(trimmed))) {
                throw new AiConfigurationException(
                        "Missing required variable '" + trimmed + "' for prompt: " + definition.getCode());
            }
        }
    }

    private List<AiMessage> buildMessages(PromptTemplate template, Map<String, String> variables) {
        var messages = new ArrayList<AiMessage>();

        if (template.getSystemPrompt() != null && !template.getSystemPrompt().isBlank()) {
            messages.add(AiMessage.builder()
                    .role(AiRole.SYSTEM)
                    .content(template.getSystemPrompt())
                    .build());
        }

        var rendered = promptRenderer.render(template.getUserPromptTemplate(), variables);
        messages.add(AiMessage.builder()
                .role(AiRole.USER)
                .content(rendered)
                .build());

        return messages;
    }

    private void validateRequiredVariables(PromptTemplate template, Map<String, String> variables) {
        var required = template.getRequiredVariables();
        if (required == null || required.isBlank()) return;

        for (var varName : required.split(",")) {
            var trimmed = varName.trim();
            if (!trimmed.isEmpty() && (variables == null || !variables.containsKey(trimmed))) {
                throw new AiConfigurationException(
                        "Missing required variable '" + trimmed + "' for prompt: " + template.getCode());
            }
        }
    }
}
