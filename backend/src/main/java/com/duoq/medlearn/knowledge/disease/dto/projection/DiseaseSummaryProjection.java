package com.duoq.medlearn.domain.dto.disease;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiseaseSummaryProjection {
    private Long id;
    private String name;
    private String slug;
    private OffsetDateTime updatedAt;
}
