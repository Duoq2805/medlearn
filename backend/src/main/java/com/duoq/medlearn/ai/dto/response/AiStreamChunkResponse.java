package com.duoq.medlearn.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStreamChunkResponse {
    private String chunk;
    private boolean done;
    private OffsetDateTime createdAt;
}
