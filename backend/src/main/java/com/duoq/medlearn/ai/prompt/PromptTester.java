package com.duoq.medlearn.ai.prompt;

import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.PromptTestResultResponse;
import com.duoq.medlearn.ai.gateway.AiGatewayRouter;
import com.duoq.medlearn.ai.model.AiModel;
import com.duoq.medlearn.ai.prompt.entity.PromptTestResult;
import com.duoq.medlearn.ai.repository.PromptTestResultRepository;
import com.duoq.medlearn.domain.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PromptTester {

    private final PromptTemplateService promptTemplateService;
    private final PromptBuilder promptBuilder;
    private final AiGatewayRouter gatewayRouter;
    private final PromptTestResultRepository testResultRepository;

    public PromptTestResultResponse test(String promptCode, String version, Map<String, String> variables,
                                     String expectedOutput, String model, Long userId) {
        var template = version != null
                ? promptTemplateService.getEntity(promptCode, version)
                : promptTemplateService.getActiveEntity(promptCode);

        var messages = promptBuilder.buildFromTemplate(template, variables);
        var request = AiChatRequest.builder()
                .messages(messages)
                .model(model != null ? model : template.getModel())
                .temperature(template.getTemperature())
                .maxTokens(template.getMaxTokens())
                .build();

        var start = System.currentTimeMillis();
        String actualOutput;
        String errorMessage = null;
        boolean passed;

        try {
            var response = gatewayRouter.chat(request, AiModel.fromModelId(request.getModel()));
            actualOutput = !response.getChoices().isEmpty()
                    ? response.getChoices().getFirst().getContent() : "";
            passed = expectedOutput == null || expectedOutput.equals(actualOutput);
        } catch (Exception e) {
            actualOutput = null;
            errorMessage = e.getMessage();
            passed = false;
        }

        var latencyMs = System.currentTimeMillis() - start;

        var result = PromptTestResult.builder()
                .promptCode(promptCode)
                .version(template.getVersion())
                .variables(variables != null ? variables.toString() : null)
                .expectedOutput(expectedOutput)
                .actualOutput(actualOutput)
                .passed(passed)
                .errorMessage(errorMessage)
                .latencyMs((int) latencyMs)
                .executedBy(userId != null ? User.builder().id(userId).build() : null)
                .build();

        var saved = testResultRepository.save(result);
        return toDto(saved);
    }

    public List<PromptTestResultResponse> getResults(String promptCode) {
        return testResultRepository.findByPromptCodeOrderByExecutedAtDesc(promptCode)
                .stream().map(this::toDto).toList();
    }

    private PromptTestResultResponse toDto(PromptTestResult entity) {
        return PromptTestResultResponse.builder()
                .id(entity.getId())
                .promptCode(entity.getPromptCode())
                .version(entity.getVersion())
                .passed(entity.isPassed())
                .errorMessage(entity.getErrorMessage())
                .latencyMs(entity.getLatencyMs() != null ? entity.getLatencyMs().longValue() : null)
                .executedAt(entity.getExecutedAt())
                .build();
    }
}
