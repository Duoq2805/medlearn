package com.duoq.medlearn.knowledge.symptom.service;

import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomMatchResult;

import java.util.List;

public interface SymptomCheckerService {
    List<SymptomMatchResult> checkSymptoms(List<Long> symptomIds, int limit);
}
