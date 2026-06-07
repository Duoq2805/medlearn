package com.duoq.medlearn.domain.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDiseaseRequest {

    @Size(max = 255, message = "Disease name must be at most 255 characters")
    private String name;

    @Size(max = 255, message = "Slug must be at most 255 characters")
    private String slug;

    private Long categoryId;
}
