package com.duoq.medlearn.knowledge.symptom.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SymptomAnalysisV2Response {
    private Long diseaseId;
    private String diseaseName;
    private String slug;
    private String icdCode;
    private double confidence;
    private double score;
    private double matchScore;
    private Severity severity;
    private String severityExplanation;
    private String clinicalExplanation;
    private String recommendation;
    private List<SymptomInfo> matchedSymptoms;
    private List<SymptomInfo> missingSymptoms;
    private List<SymptomInfo> recommendedNextSymptoms;
    private List<SymptomInfo> matchedCriticalSymptoms;
    private List<SymptomInfo> missingCriticalSymptoms;
    private List<String> redFlags;
    private int matchedCount;
    private int totalSymptoms;

    public enum Severity {
        EMERGENCY, URGENT, MODERATE, NORMAL
    }

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
