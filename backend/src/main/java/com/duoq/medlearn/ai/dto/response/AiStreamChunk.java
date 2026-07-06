package com.duoq.medlearn.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStreamChunk {
    private String id;
    private String model;
    private String content;
    private String finishReason;
    private AiUsage usage;
    private boolean cached;
}
