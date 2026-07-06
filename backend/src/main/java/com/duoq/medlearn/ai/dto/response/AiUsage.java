package com.duoq.medlearn.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiUsage {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
}
