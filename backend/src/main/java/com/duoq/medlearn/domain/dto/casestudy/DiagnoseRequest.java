package com.duoq.medlearn.domain.dto.casestudy;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DiagnoseRequest {
    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;
}
