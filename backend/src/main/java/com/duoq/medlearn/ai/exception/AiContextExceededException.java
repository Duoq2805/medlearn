package com.duoq.medlearn.ai.exception;

public class AiContextExceededException extends RuntimeException {
    private final int contextWindow;
    private final int requestedTokens;

    public AiContextExceededException(String message, int contextWindow, int requestedTokens) {
        super(message);
        this.contextWindow = contextWindow;
        this.requestedTokens = requestedTokens;
    }

    public int getContextWindow() {
        return contextWindow;
    }

    public int getRequestedTokens() {
        return requestedTokens;
    }
}
