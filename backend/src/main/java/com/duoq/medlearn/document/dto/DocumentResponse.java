package com.duoq.medlearn.document.dto;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder
public class DocumentResponse {
    Long id;
    String title;
    String fileName;
    Long fileSize;
    String mimeType;
    String sourceUrl;
    Integer pageCount;
    String status;
    String errorMessage;
    Long createdBy;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
