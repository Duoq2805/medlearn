package com.duoq.medlearn.dto;

import com.duoq.medlearn.domain.enums.DiseaseStatus;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class DiseaseSummaryDTO {
    private Long id;
    private String name;
    private String slug;
    private DiseaseStatus status;
    private OffsetDateTime updatedAt;
}