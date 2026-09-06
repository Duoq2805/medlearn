package com.duoq.medlearn.domain.dto.symptom;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSymptomRequest {
    @NotBlank(message = "Symptom name is required")
    private String name;

    private String description;
}
