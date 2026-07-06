package com.duoq.medlearn.ai.gateway;

import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;
import com.duoq.medlearn.ai.dto.response.AiStreamChunk;
import com.duoq.medlearn.ai.dto.response.ModelInfo;

import java.util.List;
import java.util.function.Consumer;

public interface AiGateway {

    AiChatResponse chat(AiChatRequest request);

    void chatStream(AiChatRequest request, Consumer<AiStreamChunk> onChunk, Runnable onComplete, Consumer<Throwable> onError);

    List<ModelInfo> listModels();

    String getProviderName();
}
