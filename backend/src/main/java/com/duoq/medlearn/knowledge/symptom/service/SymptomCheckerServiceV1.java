package com.duoq.medlearn.knowledge.symptom.service;

import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomAnalysisResponse;
import java.util.List;

public interface SymptomCheckerServiceV1 {
    List<SymptomAnalysisResponse> analyze(List<Long> symptomIds);
}
