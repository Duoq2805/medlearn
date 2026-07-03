package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.dto.symptom.CreateSymptomRequest;
import com.duoq.medlearn.domain.dto.symptom.SymptomDTO;
import com.duoq.medlearn.domain.dto.symptom.UpdateSymptomRequest;
import com.duoq.medlearn.domain.entity.Symptom;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.mapper.SymptomMapper;
import com.duoq.medlearn.repository.SymptomRepository;
import com.duoq.medlearn.service.SymptomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SymptomServiceImpl implements SymptomService {
    private final SymptomRepository symptomRepository;
    private final SymptomMapper symptomMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SymptomDTO> getAllSymptoms() {
        return symptomRepository.findAll().stream()
                .map(symptomMapper::toSymptomDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<SymptomDTO> searchSymptoms(String query) {
        if (query == null || query.isBlank()) {
            return getAllSymptoms();
        }
        String normalizedQuery = query.toLowerCase();
        return symptomRepository.findAll().stream()
                .filter(symptom -> symptom.getName().toLowerCase().contains(normalizedQuery)
                        || (symptom.getDescription() != null && symptom.getDescription().toLowerCase().contains(normalizedQuery)))
                .map(symptomMapper::toSymptomDTO)
                .toList();
    }

    @Override
    @Transactional
    public SymptomDTO createSymptom(CreateSymptomRequest request) {
        if (symptomRepository.existsByName(request.getName())) {
            throw new IllegalStateException("Symptom name already exists");
        }
        String slug = request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
        if (slug.isEmpty()) slug = "symptom-" + System.currentTimeMillis();
        if (symptomRepository.existsBySlug(slug)) {
            throw new IllegalStateException("Symptom slug already exists");
        }
        Symptom symptom = Symptom.builder()
                .name(request.getName())
                .slug(slug)
                .description(request.getDescription())
                .build();
        return symptomMapper.toSymptomDTO(symptomRepository.save(symptom));
    }

    @Override
    @Transactional
    public SymptomDTO updateSymptom(Long id, UpdateSymptomRequest request) {
        Symptom symptom = findSymptom(id);
        if (request.getName() != null) {
            if (!request.getName().equals(symptom.getName()) && symptomRepository.existsByName(request.getName())) {
                throw new IllegalStateException("Symptom name already exists");
            }
            symptom.setName(request.getName());
        }
        if (request.getDescription() != null) {
            symptom.setDescription(request.getDescription());
        }
        return symptomMapper.toSymptomDTO(symptomRepository.save(symptom));
    }

    @Override
    @Transactional
    public void deleteSymptom(Long id) {
        if (!symptomRepository.existsById(id)) {
            throw new ResourceNotFoundException("Symptom not found");
        }
        symptomRepository.deleteById(id);
    }

    private Symptom findSymptom(Long id) {
        return symptomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Symptom not found"));
    }
}
