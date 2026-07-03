package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.symptom.CreateSymptomRequest;
import com.duoq.medlearn.domain.dto.symptom.SymptomDTO;
import com.duoq.medlearn.domain.dto.symptom.UpdateSymptomRequest;

import java.util.List;

public interface SymptomService {
    List<SymptomDTO> getAllSymptoms();

    List<SymptomDTO> searchSymptoms(String query);

    SymptomDTO createSymptom(CreateSymptomRequest request);

    SymptomDTO updateSymptom(Long id, UpdateSymptomRequest request);

    void deleteSymptom(Long id);
}
