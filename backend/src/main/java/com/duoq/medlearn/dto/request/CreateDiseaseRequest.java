package com.duoq.medlearn.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateDiseaseRequest {

    @NotBlank(message = "Disease name is required")
    @Size(max = 255, message = "Disease name must be at most 255 characters")
    private String name;

    @NotBlank(message = "Slug is required")
    @Size(max = 255, message = "Slug must be at most 255 characters")
    private String slug;

    private Long categoryId;

    @Size(max = 255, message = "Initial version title must be at most 255 characters")
    private String initialVersionTitle;
}
