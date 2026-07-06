package com.duoq.medlearn.symptom.service;

import com.duoq.medlearn.symptom.dto.response.SymptomMatchResult;

import java.util.List;

public interface SymptomCheckerService {
    List<SymptomMatchResult> checkSymptoms(List<Long> symptomIds, int limit);
}
