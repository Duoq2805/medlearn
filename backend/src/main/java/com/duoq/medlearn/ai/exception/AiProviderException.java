package com.duoq.medlearn.ai.exception;

public class AiProviderException extends RuntimeException {
    private final String provider;
    private final Integer statusCode;

    public AiProviderException(String provider, String message) {
        super(message);
        this.provider = provider;
        this.statusCode = null;
    }

    public AiProviderException(String provider, String message, int statusCode) {
        super(message);
        this.provider = provider;
        this.statusCode = statusCode;
    }

    public AiProviderException(String provider, String message, Throwable cause) {
        super(message, cause);
        this.provider = provider;
        this.statusCode = null;
    }

    public String getProvider() {
        return provider;
    }

    public Integer getStatusCode() {
        return statusCode;
    }
}
