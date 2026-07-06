package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.symptom.CreateSymptomRequest;
import com.duoq.medlearn.domain.dto.symptom.SymptomResponse;
import com.duoq.medlearn.domain.dto.symptom.UpdateSymptomRequest;

import java.util.List;

public interface SymptomService {
    List<SymptomResponse> getAllSymptoms();

    SymptomResponse getSymptomById(Long id);

    List<SymptomResponse> searchSymptoms(String query);

    SymptomResponse createSymptom(CreateSymptomRequest request);

    SymptomResponse updateSymptom(Long id, UpdateSymptomRequest request);

    void deleteSymptom(Long id);
}
