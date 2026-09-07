package com.duoq.medlearn.ai.usage;

import com.duoq.medlearn.ai.usage.entity.AiUsageLog;
import com.duoq.medlearn.auth.entity.User;

public interface AiUsageService {

    void log(User user, String requestType, String model, String provider,
             int promptTokens, int completionTokens, int totalTokens,
             long latencyMs, boolean success, String errorMessage, boolean cached);

    long getTokensUsedThisMonth(Long userId);

    boolean hasQuota(Long userId, long estimatedTokens);
}
