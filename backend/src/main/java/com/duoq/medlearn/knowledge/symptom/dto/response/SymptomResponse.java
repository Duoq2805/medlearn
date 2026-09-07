package com.duoq.medlearn.knowledge.symptom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class SymptomResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
