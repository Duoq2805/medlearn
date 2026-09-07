package com.duoq.medlearn.ai.usage.impl;

import com.duoq.medlearn.ai.usage.AiUsageService;
import com.duoq.medlearn.ai.repository.AiUsageLogRepository;
import com.duoq.medlearn.ai.usage.entity.AiUsageLog;
import com.duoq.medlearn.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiUsageServiceImpl implements AiUsageService {

    private final AiUsageLogRepository usageLogRepository;

    @Override
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User user, String requestType, String model, String provider,
                    int promptTokens, int completionTokens, int totalTokens,
                    long latencyMs, boolean success, String errorMessage, boolean cached) {
        try {
            var entry = AiUsageLog.builder()
                    .user(user)
                    .requestType(requestType)
                    .model(model)
                    .provider(provider)
                    .promptTokens(promptTokens)
                    .completionTokens(completionTokens)
                    .totalTokens(totalTokens)
                    .latencyMs((int) latencyMs)
                    .success(success)
                    .errorMessage(errorMessage)
                    .cached(cached)
                    .build();
            usageLogRepository.save(entry);
        } catch (Exception e) {
            log.error("Failed to persist AI usage log: type={}, model={}", requestType, model, e);
        }
    }

    @Override
    public long getTokensUsedThisMonth(Long userId) {
        var startOfMonth = OffsetDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        return usageLogRepository.sumTokensByUserIdSince(userId, startOfMonth);
    }

    @Override
    public boolean hasQuota(Long userId, long estimatedTokens) {
        // ponytail: quota check uses user-level token tracking; add global pool check when needed
        var used = getTokensUsedThisMonth(userId);
        var monthlyLimit = 1_000_000L; // ponytail: read from User entity's monthlyTokenQuota
        return (used + estimatedTokens) <= monthlyLimit;
    }
}
