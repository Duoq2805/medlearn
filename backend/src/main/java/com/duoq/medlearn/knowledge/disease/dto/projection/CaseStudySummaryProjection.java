package com.duoq.medlearn.knowledge.disease.dto.projection;

import com.duoq.medlearn.knowledge.disease.enums.CaseDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class CaseStudySummaryProjection {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private CaseDifficulty difficulty;
    private Boolean isFeatured;
    private Long viewCount;
    private OffsetDateTime createdAt;
}
