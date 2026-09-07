package com.duoq.medlearn.ai.cache;

import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.AiChatResponse;

import java.util.Optional;

public interface AiCacheService {

    Optional<AiChatResponse> get(AiChatRequest request);

    void put(AiChatRequest request, AiChatResponse response);

    void evict(String cacheKey);

    void clear();

    long size();
}
