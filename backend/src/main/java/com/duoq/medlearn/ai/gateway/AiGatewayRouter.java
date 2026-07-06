package com.duoq.medlearn.ai.gateway;

import com.duoq.medlearn.ai.config.AiProperties;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiStreamChunk;
import com.duoq.medlearn.ai.dto.response.ModelInfo;
import com.duoq.medlearn.ai.exception.AiConfigurationException;
import com.duoq.medlearn.ai.gateway.provider.AiProvider;
import com.duoq.medlearn.ai.model.AiModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiGatewayRouter {

    private final List<AiGateway> gateways;
    private final List<AiProvider> providers;
    private final AiProperties aiProperties;

    public AiGateway resolve(String provider) {
        if (provider != null) {
            return gateways.stream()
                    .filter(g -> g.getProviderName().equals(provider))
                    .findFirst()
                    .orElseThrow(() -> new AiConfigurationException("No gateway found for provider: " + provider));
        }
        var defaultProvider = aiProperties.getGateway().getDefaultProvider();
        return gateways.stream()
                .filter(g -> g.getProviderName().equals(defaultProvider))
                .findFirst()
                .orElseThrow(() -> new AiConfigurationException("No default gateway configured"));
    }

    public AiProvider resolveProvider(AiModel model) {
        return providers.stream()
                .filter(p -> p.supportsModel(model))
                .findFirst()
                .orElseThrow(() -> new AiConfigurationException("No provider found for model: " + model));
    }

    public AiChatResponse chat(AiChatRequest request, AiModel model) {
        return resolveProvider(model).chat(request, model);
    }

    public void chatStream(AiChatRequest request, AiModel model,
                           Consumer<AiStreamChunkAdapter> onChunk,
                           Runnable onComplete, Consumer<Throwable> onError) {
        var provider = resolveProvider(model);
        provider.chatStream(request, model,
                chunk -> onChunk.accept(new AiStreamChunkAdapter(chunk, provider.getProviderName())),
                onComplete, onError);
    }

    public List<ModelInfo> listModels(String provider) {
        return resolve(provider).listModels();
    }

    public Map<String, List<ModelInfo>> listAllModels() {
        return gateways.stream()
                .collect(Collectors.toMap(
                        AiGateway::getProviderName,
                        g -> g.listModels(),
                        (a, b) -> a
                ));
    }

    public record AiStreamChunkAdapter(AiStreamChunk chunk, String provider) {}
}
