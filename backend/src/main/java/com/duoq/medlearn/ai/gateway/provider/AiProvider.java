package com.duoq.medlearn.ai.gateway.provider;

import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiStreamChunk;
import com.duoq.medlearn.ai.dto.response.ModelInfo;
import com.duoq.medlearn.ai.model.AiModel;

import java.util.List;
import java.util.function.Consumer;

public interface AiProvider {

    AiChatResponse chat(AiChatRequest request, AiModel model);

    void chatStream(AiChatRequest request, AiModel model,
                    Consumer<AiStreamChunk> onChunk, Runnable onComplete,
                    Consumer<Throwable> onError);

    List<ModelInfo> listModels();

    String getProviderName();

    boolean supportsModel(AiModel model);
}
