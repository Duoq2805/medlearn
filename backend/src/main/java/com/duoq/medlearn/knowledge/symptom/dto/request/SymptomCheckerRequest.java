package com.duoq.medlearn.symptom.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomCheckerRequest {
    @NotEmpty(message = "Symptom IDs are required")
    private List<Long> symptomIds;
    private Integer limit;
}
