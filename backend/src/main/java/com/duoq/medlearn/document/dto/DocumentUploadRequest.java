package com.duoq.medlearn.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class DocumentUploadRequest {
    @NotBlank
    String title;

    String sourceUrl;
}
