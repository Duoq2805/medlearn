package com.duoq.medlearn.knowledge.section.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionTypeResponse {
    private Integer id;
    private String name;
    private String description;
}
