package com.duoq.medlearn.symptom.service.impl;

import com.duoq.medlearn.symptom.dto.response.SymptomMatchResult;
import com.duoq.medlearn.symptom.service.SymptomCheckerService;
import com.duoq.medlearn.repository.DiseaseSectionRepository;
import com.duoq.medlearn.repository.DiseaseVersionSymptomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SymptomCheckerServiceImpl implements SymptomCheckerService {

    private final DiseaseVersionSymptomRepository repository;
    private final DiseaseSectionRepository sectionRepository;

    @Override
    public List<SymptomMatchResult> checkSymptoms(List<Long> symptomIds, int limit) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> rawResults = repository.findMatchingDiseasesRaw(symptomIds, limit);

        List<SymptomMatchResult> results = new ArrayList<>();
        for (Object[] row : rawResults) {
            Long diseaseId = ((Number) row[0]).longValue();
            String diseaseName = (String) row[1];
            String diseaseSlug = (String) row[2];
            Integer matchCount = ((Number) row[3]).intValue();
            Double totalWeight = ((Number) row[4]).doubleValue();

            Double matchScore = calculateMatchScore(matchCount, totalWeight, symptomIds.size());

            List<String> matchedSymptoms = repository.findMatchedSymptomsByDisease(diseaseId, symptomIds);

            String shortDescription = getDiseaseShortDescription(diseaseId);

            results.add(new SymptomMatchResult(
                    diseaseId, diseaseName, diseaseSlug,
                    matchCount, matchScore, matchedSymptoms, shortDescription
            ));
        }

        return results;
    }

    private Double calculateMatchScore(int matchCount, Double totalWeight, int totalSymptoms) {
        double countScore = (double) matchCount / totalSymptoms;
        double maxPossibleWeight = totalSymptoms * 1.0;
        double weightScore = totalWeight / maxPossibleWeight;
        return Math.round((countScore * 0.6 + weightScore * 0.4) * 100) / 100.0;
    }

    private String getDiseaseShortDescription(Long diseaseId) {
        return sectionRepository.findShortDescriptionByDiseaseId(diseaseId)
                .orElse("No description available");
    }
}
