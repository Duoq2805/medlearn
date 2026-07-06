package com.duoq.medlearn.symptom.service.impl;

import com.duoq.medlearn.symptom.dto.response.SymptomAnalysisResponse;
import com.duoq.medlearn.symptom.dto.response.SymptomAnalysisResponse.SymptomInfo;
import com.duoq.medlearn.symptom.service.SymptomCheckerServiceV1;
import com.duoq.medlearn.repository.DiseaseRepository;
import com.duoq.medlearn.repository.DiseaseVersionSymptomRepository;
import com.duoq.medlearn.repository.SymptomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    public List<SymptomAnalysisResponse> analyze(List<Long> symptomIds) {
        if (symptomIds == null || symptomIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> rawCandidates = dvsRepository.findMatchingDiseasesRaw(symptomIds, CANDIDATE_FETCH_LIMIT);
        if (rawCandidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> candidateIds = new ArrayList<>(rawCandidates.size());
        Map<Long, Integer> matchCountMap = new HashMap<>(rawCandidates.size());
        Map<Long, String> nameMap = new HashMap<>(rawCandidates.size());

        for (Object[] row : rawCandidates) {
            Long diseaseId = ((Number) row[0]).longValue();
            String diseaseName = (String) row[1];
            int matchCount = ((Number) row[3]).intValue();

            candidateIds.add(diseaseId);
            matchCountMap.put(diseaseId, matchCount);
            nameMap.put(diseaseId, diseaseName);
        }

        Map<Long, Long> totalCountMap = new HashMap<>(candidateIds.size());
        List<Object[]> countRows = dvsRepository.findTotalSymptomCountBatch(candidateIds);
        for (Object[] row : countRows) {
            Long diseaseId = ((Number) row[0]).longValue();
            Long totalCount = ((Number) row[1]).longValue();
            totalCountMap.put(diseaseId, totalCount);
        }

        Map<Long, List<Long>> diseaseSymptomMap = new HashMap<>(candidateIds.size());
        List<Object[]> mappingRows = dvsRepository.findDiseaseSymptomMappings(candidateIds);
        for (Object[] row : mappingRows) {
            Long diseaseId = ((Number) row[0]).longValue();
            Long symptomId = ((Number) row[1]).longValue();
            diseaseSymptomMap.computeIfAbsent(diseaseId, k -> new ArrayList<>()).add(symptomId);
        }

        Set<Long> userSymptomIds = new HashSet<>(symptomIds);

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

            List<Long> diseaseSymptomList = diseaseSymptomMap.get(diseaseId);
            if (diseaseSymptomList == null) {
                continue;
            }

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
                    Math.round(score * 10.0) / 10.0,
                    matchedSymptoms,
                    missingSymptoms,
                    explanation
            ));
        }

        entries.sort((a, b) -> Double.compare(b.score, a.score));
        List<ResultEntry> top = entries.size() > MAX_RESULTS ? entries.subList(0, MAX_RESULTS) : entries;

        return top.stream()
                .map(e -> SymptomAnalysisResponse.builder()
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
