package com.duoq.medlearn.domain.dto.ai;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomCheckerRequest {

    @NotEmpty(message = "At least one symptom ID is required")
    private List<Long> symptomIds;

    private Integer limit;
}
