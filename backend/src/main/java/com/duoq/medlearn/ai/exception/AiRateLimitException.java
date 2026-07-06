package com.duoq.medlearn.ai.exception;

public class AiRateLimitException extends RuntimeException {
    private final Long retryAfterMs;

    public AiRateLimitException(String message) {
        super(message);
        this.retryAfterMs = null;
    }

    public AiRateLimitException(String message, Long retryAfterMs) {
        super(message);
        this.retryAfterMs = retryAfterMs;
    }

    public Long getRetryAfterMs() {
        return retryAfterMs;
    }
}
