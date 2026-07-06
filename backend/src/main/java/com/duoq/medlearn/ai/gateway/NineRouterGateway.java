package com.duoq.medlearn.ai.gateway;

import com.duoq.medlearn.ai.config.AiProperties;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiStreamChunk;
import com.duoq.medlearn.ai.dto.response.ModelInfo;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.dto.enums.AiRole;
import com.duoq.medlearn.ai.dto.response.AiUsage;
import com.duoq.medlearn.ai.exception.AiProviderException;
import com.duoq.medlearn.ai.exception.AiProviderUnavailableException;
import com.duoq.medlearn.ai.exception.AiRateLimitException;
import com.duoq.medlearn.ai.model.AiModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.function.Consumer;

@Service
@ConditionalOnProperty(name = "ai.gateway.default-provider", havingValue = "nine-router", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class NineRouterGateway implements AiGateway {

    private final RestTemplate restTemplate;
    private final AiProperties aiProperties;

    @Override
    public AiChatResponse chat(AiChatRequest request) {
        var config = aiProperties.getGateway();
        var url = config.getBaseUrl() + "/v1/chat/completions";

        var body = buildRequestBody(request);
        var headers = buildHeaders(config.getApiKey());

        try {
            var entity = new HttpEntity<>(body, headers);
            var response = restTemplate.exchange(url, HttpMethod.POST, entity, NineRouterChatResponse.class);
            return mapResponse(response.getBody());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new AiRateLimitException("Rate limited by AI provider");
            }
            throw new AiProviderException("nine-router", "Provider returned " + e.getStatusCode(), e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            throw new AiProviderUnavailableException("nine-router", "Provider unreachable: " + e.getMessage());
        }
    }

    @Override
    public void chatStream(AiChatRequest request, Consumer<AiStreamChunk> onChunk,
                           Runnable onComplete, Consumer<Throwable> onError) {
        // ponytail: streaming via SSE not yet implemented; falls back to non-streaming then simulates chunks
        try {
            var nonStreaming = AiChatRequest.builder()
                    .messages(request.getMessages())
                    .model(request.getModel())
                    .temperature(request.getTemperature())
                    .maxTokens(request.getMaxTokens())
                    .stream(false)
                    .build();
            var response = chat(nonStreaming);
            if (!response.getChoices().isEmpty()) {
                var content = response.getChoices().getFirst().getContent();
                onChunk.accept(AiStreamChunk.builder()
                        .id(response.getId())
                        .model(response.getModel())
                        .content(content)
                        .finishReason("stop")
                        .usage(response.getUsage() != null ? AiUsage.builder()
                                .promptTokens(response.getUsage().getPromptTokens())
                                .completionTokens(response.getUsage().getCompletionTokens())
                                .totalTokens(response.getUsage().getTotalTokens())
                                .build() : null)
                        .build());
            }
            onComplete.run();
        } catch (Exception e) {
            onError.accept(e);
        }
    }

    @Override
    public List<ModelInfo> listModels() {
        var config = aiProperties.getGateway();
        var url = config.getBaseUrl() + "/v1/models";
        var headers = buildHeaders(config.getApiKey());

        try {
            var entity = new HttpEntity<>(headers);
            var response = restTemplate.exchange(url, HttpMethod.GET, entity, NineRouterModelsResponse.class);
            return response.getBody().data().stream()
                    .map(m -> ModelInfo.builder()
                            .id(m.id())
                            .name(m.id())
                            .provider("nine-router")
                            .build())
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to list models from nine-router", e);
            return List.of();
        }
    }

    @Override
    public String getProviderName() {
        return "nine-router";
    }

    private HttpHeaders buildHeaders(String apiKey) {
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        return headers;
    }

    private NineRouterChatRequest buildRequestBody(AiChatRequest request) {
        var messages = request.getMessages().stream()
                .map(m -> new NineRouterMessage(m.getRole().name().toLowerCase(), m.getContent()))
                .toList();
        var model = AiModel.fromModelId(request.getModel());
        if (model == null) {
            model = AiModel.GPT_5_MINI;
        }
        return new NineRouterChatRequest(
                model.getModelId(),
                messages,
                request.getTemperature(),
                request.getMaxTokens(),
                false
        );
    }

    private AiChatResponse mapResponse(NineRouterChatResponse resp) {
        if (resp == null || resp.choices() == null || resp.choices().isEmpty()) {
            return AiChatResponse.builder()
                    .model("unknown")
                    .provider("nine-router")
                    .build();
        }
        var choice = resp.choices().getFirst();
        var messages = List.of(AiMessage.builder()
                .role(AiRole.ASSISTANT)
                .content(choice.message() != null ? choice.message().content() : "")
                .build());
        var usage = resp.usage() != null ? AiUsage.builder()
                .promptTokens(resp.usage().promptTokens())
                .completionTokens(resp.usage().completionTokens())
                .totalTokens(resp.usage().totalTokens())
                .build() : null;
        return AiChatResponse.builder()
                .id(resp.id())
                .model(resp.model())
                .provider("nine-router")
                .choices(messages)
                .usage(usage)
                .build();
    }

    private record NineRouterChatRequest(String model, List<NineRouterMessage> messages,
                                          Double temperature, Integer maxTokens, boolean stream) {}

    private record NineRouterMessage(String role, String content) {}

    private record NineRouterChatResponse(String id, String model, List<NineRouterChoice> choices,
                                           NineRouterUsage usage) {}

    private record NineRouterChoice(NineRouterMessage message, String finishReason) {}

    private record NineRouterUsage(int promptTokens, int completionTokens, int totalTokens) {}

    private record NineRouterModelsResponse(List<NineRouterModel> data) {}

    private record NineRouterModel(String id, String object) {}
}
