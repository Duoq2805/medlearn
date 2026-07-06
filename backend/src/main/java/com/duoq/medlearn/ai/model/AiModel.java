package com.duoq.medlearn.ai.model;

import lombok.Getter;

@Getter
public enum AiModel {
    GPT_5("gpt-5"),
    GPT_5_MINI("gpt-5-mini"),
    CLAUDE_4_OPUS("claude-4-opus"),
    CLAUDE_4_SONNET("claude-4-sonnet"),
    GEMINI_2_5_PRO("gemini-2.5-pro"),
    GEMINI_2_5_FLASH("gemini-2.5-flash"),
    DEEPSEEK_CHAT("deepseek-chat"),
    QWEN_MAX("qwen-max"),
    OLLAMA_LOCAL("ollama-local"),
    CUSTOM("custom");

    private final String modelId;

    AiModel(String modelId) {
        this.modelId = modelId;
    }

    public static AiModel fromModelId(String modelId) {
        if (modelId == null || modelId.isBlank()) {
            return null;
        }
        for (AiModel model : values()) {
            if (model.modelId.equals(modelId)) {
                return model;
            }
        }
        return CUSTOM;
    }
}
