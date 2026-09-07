package com.duoq.medlearn.document.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DocumentChunkResponse {
    Long id;
    Integer chunkIndex;
    String content;
    Integer charCount;
    Integer pageNumber;
    String heading;
}
