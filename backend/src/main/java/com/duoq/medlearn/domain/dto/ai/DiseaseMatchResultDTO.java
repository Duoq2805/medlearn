package com.duoq.medlearn.domain.dto.ai;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Result DTO for symptom checker V1 analyze endpoint.
 * Contains deterministic match score and symptom details.
 */
@Data
@Builder
@AllArgsConstructor
public class DiseaseMatchResultDTO {
    private Long diseaseId;
    private String diseaseName;
    private Double matchScore;
    private List<SymptomInfo> matchedSymptoms;
    private List<SymptomInfo> missingSymptoms;
    private String explanation;

    @Data
    @Builder
    @AllArgsConstructor
    public static class SymptomInfo {
        private Long id;
        private String name;
    }
}