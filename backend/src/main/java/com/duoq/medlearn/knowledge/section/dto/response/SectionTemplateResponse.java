package com.duoq.medlearn.domain.dto.section;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionTemplateResponse {
    private String sectionType;
    private String title;
    private String template;
}
