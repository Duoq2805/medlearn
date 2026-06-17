package com.duoq.medlearn.domain.dto.ai;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class AiStreamChunkResponse {
    private String chunk;
    private boolean done;
    private OffsetDateTime createdAt;
}
