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
public class SymptomMatchResult {
    private Long diseaseId;
    private String diseaseName;
    private String slug;
    private int matchedCount;
    private double score;
    private List<String> matchedSymptoms;
    private String shortDescription;
}
