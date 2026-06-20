package com.duoq.medlearn.domain.dto.ai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for symptom checker V1 analyze endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomCheckerRequest {
    private List<Long> symptomIds;
}