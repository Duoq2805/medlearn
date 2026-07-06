package com.duoq.medlearn.ai.security;

import com.duoq.medlearn.ai.config.AiProperties;
import com.duoq.medlearn.ai.exception.AiQuotaExceededException;
import com.duoq.medlearn.ai.usage.AiUsageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiQuotaService {

    private final AiUsageService usageService;
    private final AiProperties aiProperties;

    public void checkQuota(Long userId, long estimatedTokens) {
        if (!aiProperties.getQuota().isEnabled()) return;

        var hasQuota = usageService.hasQuota(userId, estimatedTokens);
        if (!hasQuota) {
            log.warn("Quota exceeded for user={}, estimatedTokens={}", userId, estimatedTokens);
            throw new AiQuotaExceededException("Monthly token quota exceeded");
        }
    }
}
