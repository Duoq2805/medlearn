package com.duoq.medlearn.symptom.service;

import com.duoq.medlearn.symptom.dto.response.SymptomAnalysisResponse;
import java.util.List;

public interface SymptomCheckerServiceV1 {
    List<SymptomAnalysisResponse> analyze(List<Long> symptomIds);
}
