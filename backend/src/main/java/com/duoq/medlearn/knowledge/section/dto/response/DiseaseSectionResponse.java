package com.duoq.medlearn.knowledge.section.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiseaseSectionResponse {
    private Long id;
    private Long diseaseVersionId;
    private Integer sectionTypeId;
    private String sectionTypeName;
    private String title;
    private String content;
    private Integer orderIndex;
}
