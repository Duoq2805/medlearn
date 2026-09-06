package com.duoq.medlearn.symptom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymptomAnalysisResponse {
    private Long diseaseId;
    private String diseaseName;
    private String slug;
    private String icdCode;
    private double matchScore;
    private List<SymptomInfo> matchedSymptoms;
    private List<SymptomInfo> missingSymptoms;
    private String explanation;
    private double score;
    private int matchedCount;
    private int totalSymptoms;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SymptomInfo {
        private Long id;
        private String name;
        private String description;

        public SymptomInfo(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
