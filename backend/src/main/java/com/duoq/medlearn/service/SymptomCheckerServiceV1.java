package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.ai.DiseaseMatchResultDTO;
import java.util.List;

public interface SymptomCheckerServiceV1 {
    List<DiseaseMatchResultDTO> analyze(List<Long> symptomIds);
}
