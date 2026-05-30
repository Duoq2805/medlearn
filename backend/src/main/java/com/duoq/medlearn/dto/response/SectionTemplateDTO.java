package com.duoq.medlearn.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionTemplateDTO {
    private String sectionType;
    private String title;
    private String template;
}
