package com.duoq.medlearn.knowledge.section.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDiseaseSectionRequest {

    private Integer sectionTypeId;

    @Size(max = 255, message = "Section title must be at most 255 characters")
    private String title;

    private String content;

    private Integer orderIndex;
}
