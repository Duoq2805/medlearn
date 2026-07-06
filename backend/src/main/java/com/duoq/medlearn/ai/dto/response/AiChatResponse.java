package com.duoq.medlearn.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String id;
    private String model;
    private String provider;
    private List<AiMessage> choices;
    private AiUsage usage;
    private boolean cached;
    private long latencyMs;
}
