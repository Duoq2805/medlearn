package com.duoq.medlearn.domain.dto.disease;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class DiseaseSummaryDTO {
    private Long id;
    private String name;
    private String slug;
    private OffsetDateTime updatedAt;
}