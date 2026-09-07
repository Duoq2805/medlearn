package com.duoq.medlearn.knowledge.symptom.service;

import com.duoq.medlearn.knowledge.symptom.dto.request.CreateSymptomRequest;
import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomResponse;
import com.duoq.medlearn.knowledge.symptom.dto.request.UpdateSymptomRequest;

import java.util.List;

public interface SymptomService {
    List<SymptomResponse> getAllSymptoms();

    SymptomResponse getSymptomById(Long id);

    List<SymptomResponse> searchSymptoms(String query);

    SymptomResponse createSymptom(CreateSymptomRequest request);

    SymptomResponse updateSymptom(Long id, UpdateSymptomRequest request);

    void deleteSymptom(Long id);
}
