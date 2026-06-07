package com.duoq.medlearn.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionTemplateDTO {
    private String sectionType;
    private String title;
    private String template;
}
