package com.duoq.medlearn.ai.security;

import com.duoq.medlearn.ai.config.AiProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiRateLimiterTest {

    @Mock
    private AiProperties aiProperties;

    @Mock
    private AiProperties.RateLimit rateLimitConfig;

    private AiRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        when(aiProperties.getRateLimit()).thenReturn(rateLimitConfig);
        when(rateLimitConfig.isEnabled()).thenReturn(true);
        when(rateLimitConfig.getRequestsPerMinute()).thenReturn(5);
        rateLimiter = new AiRateLimiter(aiProperties);
        rateLimiter.init();
    }

    @Test
    void checkRequestLimit_shouldAllowWithinLimit() {
        assertThatCode(() -> rateLimiter.checkRequestLimit("user-1")).doesNotThrowAnyException();
        assertThatCode(() -> rateLimiter.checkRequestLimit("user-1")).doesNotThrowAnyException();
    }

    @Test
    void checkRequestLimit_shouldThrowWhenExceeded() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.checkRequestLimit("user-1");
        }
        assertThatThrownBy(() -> rateLimiter.checkRequestLimit("user-1"))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiRateLimitException.class);
    }

    @Test
    void checkRequestLimit_shouldTrackUsersIndependently() {
        for (int i = 0; i < 10; i++) {
            try {
                rateLimiter.checkRequestLimit("user-a");
            } catch (com.duoq.medlearn.ai.exception.AiRateLimitException e) {
                // expected after 5th call
            }
        }
        assertThatCode(() -> rateLimiter.checkRequestLimit("user-b")).doesNotThrowAnyException();
    }

    @Test
    void reset_shouldClearCounter() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.checkRequestLimit("user-1");
        }
        assertThatThrownBy(() -> rateLimiter.checkRequestLimit("user-1"))
                .isInstanceOf(com.duoq.medlearn.ai.exception.AiRateLimitException.class);
        rateLimiter.reset("user-1");
        assertThatCode(() -> rateLimiter.checkRequestLimit("user-1")).doesNotThrowAnyException();
    }
}
