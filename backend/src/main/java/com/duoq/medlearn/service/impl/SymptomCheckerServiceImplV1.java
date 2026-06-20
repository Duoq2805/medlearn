package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.dto.ai.DiseaseMatchResultDTO;
import com.duoq.medlearn.domain.dto.ai.DiseaseMatchResultDTO.SymptomInfo;
import com.duoq.medlearn.repository.DiseaseRepository;
import com.duoq.medlearn.repository.DiseaseVersionSymptomRepository;
import com.duoq.medlearn.repository.SymptomRepository;
import com.duoq.medlearn.service.SymptomCheckerServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Symptom Checker V1 — deterministic rule-based engine.
 * <p>
 * Algorithm:
 * <ol>
 *   <li>Match user-provided symptom IDs against approved disease versions only.</li>
 *   <li>Score = matchedCount / totalDiseaseSymptoms × 100.</li>
 *   <li>Include only matches with score ≥ 20%.</li>
 *   <li>Return top 10 sorted by score descending.</li>
 * </ol>
 * <p>
 * Complexity: O(C + D + S) where C = candidate diseases, D = total symptom mappings, S = user symptoms.
 * Query count: 3 (find candidates, batch count, batch mappings) — no N+1.
 */
@Service
@RequiredArgsConstructor
public class SymptomCheckerServiceImplV1 implements SymptomCheckerServiceV1 {

    private static final double MIN_SCORE = 20.0;
    private static final int MAX_RESULTS = 10;
    private static final int CANDIDATE_FETCH_LIMIT = 100;

    private final DiseaseVersionSymptomRepository dvsRepository;
    private final DiseaseRepository diseaseRepository;
    private final SymptomRepository symptomRepository;

    @Override
    public List<DiseaseMatchResultDTO> analyze(List<Long> symptomIds) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. Fetch candidate diseases using existing native query (ordered by match descending)
        List<Object[]> rawCandidates = dvsRepository.findMatchingDiseasesRaw(symptomIds, CANDIDATE_FETCH_LIMIT);
        if (rawCandidates.isEmpty()) {
            return Collections.emptyList();
        }

        // Collect candidate disease IDs
        List<Long> candidateIds = new ArrayList<>(rawCandidates.size());
        // Map diseaseId -> matchCount from user symptoms
        Map<Long, Integer> matchCountMap = new HashMap<>(rawCandidates.size());
        // Map diseaseId -> diseaseName
        Map<Long, String> nameMap = new HashMap<>(rawCandidates.size());

        for (Object[] row : rawCandidates) {
            Long diseaseId = ((Number) row[0]).longValue();
            String diseaseName = (String) row[1];
            int matchCount = ((Number) row[3]).intValue();

            candidateIds.add(diseaseId);
            matchCountMap.put(diseaseId, matchCount);
            nameMap.put(diseaseId, diseaseName);
        }

        // 2. Batch fetch total symptom count per disease (1 query)
        Map<Long, Long> totalCountMap = new HashMap<>(candidateIds.size());
        List<Object[]> countRows = dvsRepository.findTotalSymptomCountBatch(candidateIds);
        for (Object[] row : countRows) {
            Long diseaseId = ((Number) row[0]).longValue();
            Long totalCount = ((Number) row[1]).longValue();
            totalCountMap.put(diseaseId, totalCount);
        }

        // 3. Batch fetch all disease → symptom mappings (1 query)
        Map<Long, List<Long>> diseaseSymptomMap = new HashMap<>(candidateIds.size());
        List<Object[]> mappingRows = dvsRepository.findDiseaseSymptomMappings(candidateIds);
        for (Object[] row : mappingRows) {
            Long diseaseId = ((Number) row[0]).longValue();
            Long symptomId = ((Number) row[1]).longValue();
            diseaseSymptomMap.computeIfAbsent(diseaseId, k -> new ArrayList<>()).add(symptomId);
        }

        // 4. Build a lookup set of user symptom IDs
        Set<Long> userSymptomIds = new HashSet<>(symptomIds);

        // 5. Batch fetch ALL symptom names across candidate diseases (1 query, no N+1)
        Set<Long> allDiseaseSymptomIds = new HashSet<>();
        for (List<Long> ids : diseaseSymptomMap.values()) {
            allDiseaseSymptomIds.addAll(ids);
        }
        Map<Long, String> allSymptomNameMap;
        if (!allDiseaseSymptomIds.isEmpty()) {
            allSymptomNameMap = symptomRepository.findAllByIdIn(new ArrayList<>(allDiseaseSymptomIds))
                    .stream()
                    .collect(Collectors.toMap(
                            com.duoq.medlearn.domain.entity.Symptom::getId,
                            com.duoq.medlearn.domain.entity.Symptom::getName
                    ));
        } else {
            allSymptomNameMap = Collections.emptyMap();
        }

        // 6. Compute score per disease, apply threshold, build results
        List<ResultEntry> entries = new ArrayList<>();

        for (Long diseaseId : candidateIds) {
            Long totalCount = totalCountMap.get(diseaseId);
            if (totalCount == null || totalCount == 0) {
                continue;
            }

            Integer matchedCount = matchCountMap.get(diseaseId);
            if (matchedCount == null || matchedCount == 0) {
                continue;
            }

            double score = (double) matchedCount / totalCount * 100.0;
            if (score < MIN_SCORE) {
                continue;
            }

            // Build matched and missing symptom lists
            List<Long> diseaseSymptomList = diseaseSymptomMap.get(diseaseId);
            if (diseaseSymptomList == null) {
                continue;
            }

            // Use Set for O(1) membership test
            Set<Long> matchedSet = new HashSet<>();
            List<SymptomInfo> matchedSymptoms = new ArrayList<>(diseaseSymptomList.size());
            List<SymptomInfo> missingSymptoms = new ArrayList<>();

            for (Long symptomId : diseaseSymptomList) {
                String name = allSymptomNameMap.get(symptomId);
                if (name == null) {
                    missingSymptoms.add(new SymptomInfo(symptomId, "Unknown symptom"));
                    continue;
                }
                if (userSymptomIds.contains(symptomId)) {
                    matchedSet.add(symptomId);
                    matchedSymptoms.add(new SymptomInfo(symptomId, name));
                }
            }

            // Missing symptoms: all disease symptoms not matched
            for (Long diseaseSymptomId : diseaseSymptomList) {
                if (!matchedSet.contains(diseaseSymptomId)) {
                    String name = allSymptomNameMap.getOrDefault(diseaseSymptomId, "Unknown symptom");
                    missingSymptoms.add(new SymptomInfo(diseaseSymptomId, name));
                }
            }

            String explanation = "Matched " + matchedCount + " of " + totalCount + " known symptoms";

            entries.add(new ResultEntry(
                    diseaseId,
                    nameMap.get(diseaseId),
                    Math.round(score * 10.0) / 10.0,  // Round to 1 decimal
                    matchedSymptoms,
                    missingSymptoms,
                    explanation
            ));
        }

        // 7. Sort by score descending, limit to 10
        entries.sort((a, b) -> Double.compare(b.score, a.score));
        List<ResultEntry> top = entries.size() > MAX_RESULTS ? entries.subList(0, MAX_RESULTS) : entries;

        // 8. Convert to DTO
        return top.stream()
                .map(e -> DiseaseMatchResultDTO.builder()
                        .diseaseId(e.diseaseId)
                        .diseaseName(e.diseaseName)
                        .matchScore(e.score)
                        .matchedSymptoms(e.matchedSymptoms)
                        .missingSymptoms(e.missingSymptoms)
                        .explanation(e.explanation)
                        .build())
                .toList();
    }

    private record ResultEntry(
            Long diseaseId,
            String diseaseName,
            Double score,
            List<SymptomInfo> matchedSymptoms,
            List<SymptomInfo> missingSymptoms,
            String explanation
    ) {}
}
