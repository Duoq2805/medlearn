package com.duoq.medlearn.domain.dto.casestudy;

import com.duoq.medlearn.domain.enums.CaseDifficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
public class CaseStudySummaryDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private CaseDifficulty difficulty;
    private Boolean isFeatured;
    private Long viewCount;
    private OffsetDateTime createdAt;
}
