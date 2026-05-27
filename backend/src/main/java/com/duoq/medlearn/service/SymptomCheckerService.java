package com.duoq.medlearn.service;

import com.duoq.medlearn.dto.response.SymptomMatchResult;

import java.util.List;

public interface SymptomCheckerService {
    List<SymptomMatchResult> checkSymptoms(List<Long> symptomIds, int limit);
}
