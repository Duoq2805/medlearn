package com.duoq.medlearn.domain.dto.symptom;

import lombok.Data;

@Data
public class UpdateSymptomRequest {
    private String name;
    private String description;
}
