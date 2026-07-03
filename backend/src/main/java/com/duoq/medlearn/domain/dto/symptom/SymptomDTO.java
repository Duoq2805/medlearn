package com.duoq.medlearn.domain.dto.symptom;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class SymptomDTO {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
