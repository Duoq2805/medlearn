package com.duoq.medlearn.dto.response;

import com.duoq.medlearn.domain.enums.DiseaseStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class DiseaseDTO {
    private Long id;
    private String name;
    private String slug;
    private Long categoryId;
    private String categoryName;
    private Long currentVersionId;
    private DiseaseStatus status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
