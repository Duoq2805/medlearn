package com.duoq.medlearn.service.impl;


import com.duoq.medlearn.dto.response.SymptomMatchResult;
import com.duoq.medlearn.repository.DiseaseSectionRepository;
import com.duoq.medlearn.repository.DiseaseVersionSymptomRepository;
import com.duoq.medlearn.service.SymptomCheckerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SymptomCheckerServiceImpl implements SymptomCheckerService {

    private final DiseaseVersionSymptomRepository repository;
    private final DiseaseSectionRepository sectionRepository;

    @Override
    public List<SymptomMatchResult> checkSymptoms(List<Long> symptomIds, int limit) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Lấy danh sách disease match
        List<Object[]> rawResults = repository.findMatchingDiseasesRaw(symptomIds, limit);

        // 2. Chuyển đổi sang DTO
        List<SymptomMatchResult> results = new ArrayList<>();
        for (Object[] row : rawResults) {
            Long diseaseId = ((Number) row[0]).longValue();
            String diseaseName = (String) row[1];
            String diseaseSlug = (String) row[2];
            Integer matchCount = ((Number) row[3]).intValue();
            Double totalWeight = ((Number) row[4]).doubleValue();

            // Tính score cuối cùng (có thể điều chỉnh công thức)
            Double matchScore = calculateMatchScore(matchCount, totalWeight, symptomIds.size());

            // Lấy danh sách symptom đã match
            List<String> matchedSymptoms = repository.findMatchedSymptomsByDisease(diseaseId, symptomIds);

            // Lấy mô tả ngắn (lấy từ section "definition")
            String shortDescription = getDiseaseShortDescription(diseaseId);

            results.add(new SymptomMatchResult(
                    diseaseId, diseaseName, diseaseSlug,
                    matchCount, matchScore, matchedSymptoms, shortDescription
            ));
        }

        return results;
    }

    private Double calculateMatchScore(int matchCount, Double totalWeight, int totalSymptoms) {
        // Công thức: (matchCount/totalSymptoms) * 0.6 + (totalWeight/maxWeight) * 0.4
        double countScore = (double) matchCount / totalSymptoms;
        double maxPossibleWeight = totalSymptoms * 1.0; // weight_score mặc định là 1.0
        double weightScore = totalWeight / maxPossibleWeight;
        return Math.round((countScore * 0.6 + weightScore * 0.4) * 100) / 100.0;
    }

    private String getDiseaseShortDescription(Long diseaseId) {
        // Lấy section có section_type = 'definition' của current version
        return sectionRepository.findShortDescriptionByDiseaseId(diseaseId)
                .orElse("No description available");
    }
}
