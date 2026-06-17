package com.duoq.medlearn.domain.dto.section;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDiseaseSectionRequest {

    @NotNull(message = "Section type id is required")
    private Integer sectionTypeId;

    @NotBlank(message = "Section title is required")
    @Size(max = 255, message = "Section title must be at most 255 characters")
    private String title;

    private String content;

    private Integer orderIndex;
}
