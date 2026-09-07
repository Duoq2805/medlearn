package com.duoq.medlearn.knowledge.section.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionTemplateResponse {
    private String sectionType;
    private String title;
    private String template;
}
