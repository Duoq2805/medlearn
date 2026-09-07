package com.duoq.medlearn.knowledge.symptom.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateSymptomRequest {
    @NotBlank(message = "Symptom name is required")
    private String name;

    private String description;
}
