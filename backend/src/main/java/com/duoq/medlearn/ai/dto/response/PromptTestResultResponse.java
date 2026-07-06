package com.duoq.medlearn.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTestResultResponse {
    private Long id;
    private String promptCode;
    private String version;
    private Map<String, String> variables;
    private String expectedOutput;
    private String actualOutput;
    private boolean passed;
    private String errorMessage;
    private Long latencyMs;
    private OffsetDateTime executedAt;
}
