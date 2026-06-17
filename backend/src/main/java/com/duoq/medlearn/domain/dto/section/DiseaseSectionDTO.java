package com.duoq.medlearn.domain.dto.section;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiseaseSectionDTO {
    private Long id;
    private Long diseaseVersionId;
    private Integer sectionTypeId;
    private String sectionTypeName;
    private String title;
    private String content;
    private Integer orderIndex;
}
