package com.duoq.medlearn.knowledge.symptom.service.impl;

import com.duoq.medlearn.knowledge.symptom.dto.request.CreateSymptomRequest;
import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomResponse;
import com.duoq.medlearn.knowledge.symptom.dto.request.UpdateSymptomRequest;
import com.duoq.medlearn.knowledge.symptom.entity.Symptom;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.knowledge.symptom.mapper.SymptomMapper;
import com.duoq.medlearn.knowledge.symptom.repository.SymptomRepository;
import com.duoq.medlearn.knowledge.symptom.service.SymptomService;
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
    public List<SymptomResponse> getAllSymptoms() {
        return symptomRepository.findAll().stream()
                .map(symptomMapper::toSymptomResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SymptomResponse getSymptomById(Long id) {
        return symptomMapper.toSymptomResponse(findSymptom(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SymptomResponse> searchSymptoms(String query) {
        if (query == null || query.isBlank()) {
            return getAllSymptoms();
        }
        String normalizedQuery = query.toLowerCase();
        return symptomRepository.findAll().stream()
                .filter(symptom -> symptom.getName().toLowerCase().contains(normalizedQuery)
                        || (symptom.getDescription() != null && symptom.getDescription().toLowerCase().contains(normalizedQuery)))
                .map(symptomMapper::toSymptomResponse)
                .toList();
    }

    @Override
    @Transactional
    public SymptomResponse createSymptom(CreateSymptomRequest request) {
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
        return symptomMapper.toSymptomResponse(symptomRepository.save(symptom));
    }

    @Override
    @Transactional
    public SymptomResponse updateSymptom(Long id, UpdateSymptomRequest request) {
        Symptom symptom = findSymptom(id);
        if (request.getName() != null) {
            if (!request.getName().equals(symptom.getName()) && symptomRepository.existsByName(request.getName())) {
                throw new IllegalStateException("Symptom name already exists");
            }
            symptom.setName(request.getName());
            String slug = request.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("^-|-$", "");
            if (slug.isEmpty()) slug = "symptom-" + System.currentTimeMillis();
            if (!slug.equals(symptom.getSlug()) && symptomRepository.existsBySlug(slug)) {
                throw new IllegalStateException("Symptom slug already exists");
            }
            symptom.setSlug(slug);
        }
        if (request.getDescription() != null) {
            symptom.setDescription(request.getDescription());
        }
        return symptomMapper.toSymptomResponse(symptomRepository.save(symptom));
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
