package com.duoq.medlearn.ai.exception;

public class AiProviderUnavailableException extends RuntimeException {
    private final String provider;

    public AiProviderUnavailableException(String provider, String message) {
        super(message);
        this.provider = provider;
    }

    public String getProvider() {
        return provider;
    }
}
