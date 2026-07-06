package com.duoq.medlearn.ai.security;

import com.duoq.medlearn.ai.config.AiProperties;
import com.duoq.medlearn.ai.exception.AiRateLimitException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiRateLimiter {

    private final AiProperties aiProperties;
    private final Map<String, SlidingWindowCounter> counters = new ConcurrentHashMap<>();

    @PostConstruct
    void init() {
        log.info("AI rate limiter initialized: requestsPerMinute={}, tokensPerMinute={}",
                aiProperties.getRateLimit().getRequestsPerMinute(),
                aiProperties.getRateLimit().getTokensPerMinute());
    }

    public void checkRequestLimit(String userId) {
        if (!aiProperties.getRateLimit().isEnabled()) return;

        var counter = counters.computeIfAbsent(userId, k -> new SlidingWindowCounter());
        var count = counter.increment();

        if (count > aiProperties.getRateLimit().getRequestsPerMinute()) {
            log.warn("Rate limit exceeded for user={}, requests={}", userId, count);
            throw new AiRateLimitException("Too many requests. Please try again later.");
        }
    }

    public void reset(String userId) {
        counters.remove(userId);
    }

    private static class SlidingWindowCounter {
        private final AtomicInteger count = new AtomicInteger(0);
        private final long windowStart = System.currentTimeMillis();

        int increment() {
            return count.incrementAndGet();
        }
    }
}
