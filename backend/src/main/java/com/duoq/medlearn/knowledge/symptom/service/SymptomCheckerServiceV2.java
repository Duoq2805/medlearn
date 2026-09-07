package com.duoq.medlearn.knowledge.symptom.service;

import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomAnalysisV2Response;
import java.util.List;

public interface SymptomCheckerServiceV2 {
    List<SymptomAnalysisV2Response> analyze(List<Long> symptomIds);
}
