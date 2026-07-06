package com.duoq.medlearn.symptom.service.impl;

import com.duoq.medlearn.symptom.dto.response.SymptomAnalysisV2Response;
import com.duoq.medlearn.symptom.dto.response.SymptomAnalysisV2Response.SymptomInfo;
import com.duoq.medlearn.symptom.service.SymptomCheckerServiceV2;
import com.duoq.medlearn.repository.DiseaseVersionSymptomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SymptomCheckerServiceImplV2 implements SymptomCheckerServiceV2 {

    private static final double MIN_CONFIDENCE = 20.0;
    private static final int MAX_RESULTS = 10;
    private static final int CANDIDATE_FETCH_LIMIT = 100;

    private final DiseaseVersionSymptomRepository dvsRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SymptomAnalysisV2Response> analyze(List<Long> symptomIds) {
        Set<Long> userSymptomIds = symptomIds == null ? Set.of() : symptomIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (userSymptomIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<Object[]> rawCandidates = dvsRepository.findMatchingDiseasesRaw(new ArrayList<>(userSymptomIds), CANDIDATE_FETCH_LIMIT);
        if (rawCandidates.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> candidateIds = rawCandidates.stream()
                .map(row -> ((Number) row[0]).longValue())
                .distinct()
                .toList();
        Map<Long, DiseaseBucket> buckets = new LinkedHashMap<>();
        for (Object[] row : dvsRepository.findDiseaseSymptomDetailsForV2(candidateIds)) {
            Long diseaseId = ((Number) row[0]).longValue();
            DiseaseBucket bucket = buckets.computeIfAbsent(diseaseId, id -> new DiseaseBucket(id, (String) row[1]));
            bucket.symptoms.add(new WeightedSymptom(
                    ((Number) row[2]).longValue(),
                    (String) row[3],
                    toDouble(row[4])
            ));
        }

        List<DiseaseResult> results = new ArrayList<>();
        for (Long diseaseId : candidateIds) {
            DiseaseBucket bucket = buckets.get(diseaseId);
            if (bucket == null || bucket.symptoms.isEmpty()) {
                continue;
            }
            DiseaseResult result = buildResult(bucket, userSymptomIds);
            if (result.confidence >= MIN_CONFIDENCE) {
                results.add(result);
            }
        }

        results.sort(Comparator
                .comparingDouble(DiseaseResult::confidence).reversed()
                .thenComparing((DiseaseResult r) -> r.matchedSymptoms.size(), Comparator.reverseOrder())
                .thenComparingDouble(DiseaseResult::averageMatchedWeight).reversed()
                .thenComparing(DiseaseResult::diseaseName));

        return results.stream()
                .limit(MAX_RESULTS)
                .map(DiseaseResult::toDto)
                .toList();
    }

    private DiseaseResult buildResult(DiseaseBucket bucket, Set<Long> userSymptomIds) {
        double totalWeight = bucket.symptoms.stream().mapToDouble(WeightedSymptom::weight).sum();
        double maxWeight = bucket.symptoms.stream().mapToDouble(WeightedSymptom::weight).max().orElse(1.0);
        double criticalThreshold = Math.max(3.0, maxWeight * 0.8);
        List<WeightedSymptom> matched = bucket.symptoms.stream().filter(s -> userSymptomIds.contains(s.id())).toList();
        List<WeightedSymptom> missing = bucket.symptoms.stream().filter(s -> !userSymptomIds.contains(s.id())).toList();
        List<WeightedSymptom> matchedCritical = matched.stream().filter(s -> s.weight() >= criticalThreshold).toList();
        List<WeightedSymptom> missingCritical = missing.stream().filter(s -> s.weight() >= criticalThreshold).toList();
        double matchedWeight = matched.stream().mapToDouble(WeightedSymptom::weight).sum();
        double coverage = matchedWeight / totalWeight;
        double averageWeight = matched.stream().mapToDouble(WeightedSymptom::weight).average().orElse(0.0) / maxWeight;
        long criticalTotal = bucket.symptoms.stream().filter(s -> s.weight() >= criticalThreshold).count();
        double criticalCoverage = criticalTotal == 0 ? 1.0 : (double) matchedCritical.size() / criticalTotal;
        double specificity = 1.0 / Math.sqrt(bucket.symptoms.size());
        int confidence = coverage == 1.0 ? 100 : clamp((int) Math.round((coverage * 55.0) + (averageWeight * 20.0) + (criticalCoverage * 15.0) + (specificity * 10.0)));
        return new DiseaseResult(
                bucket.id,
                bucket.name,
                confidence,
                round(matchedWeight / totalWeight * 100.0),
                matched.stream().map(WeightedSymptom::toInfo).toList(),
                missing.stream().map(WeightedSymptom::toInfo).toList(),
                missing.stream().sorted(Comparator.comparingDouble(WeightedSymptom::weight).reversed().thenComparing(WeightedSymptom::name)).limit(3).map(WeightedSymptom::toInfo).toList(),
                matchedCritical.stream().map(WeightedSymptom::toInfo).toList(),
                missingCritical.stream().map(WeightedSymptom::toInfo).toList(),
                averageWeight * maxWeight,
                explain(matched.size(), bucket.symptoms.size(), !matchedCritical.isEmpty(), confidence),
                detectSeverity(userSymptomIds, matched)
        );
    }

    private static String explain(int matchedCount, int totalCount, boolean criticalPresent, int confidence) {
        String band = confidence >= 75 ? "high" : confidence >= 50 ? "moderate" : "low";
        String critical = criticalPresent ? "Critical symptoms are present." : "Critical symptoms are not present.";
        return "Patient matches " + matchedCount + " of " + totalCount + " characteristic symptoms. " + critical + " Confidence is " + band + ".";
    }

    private static SeverityResult detectSeverity(Set<Long> symptomIds, List<WeightedSymptom> matched) {
        Set<String> names = matched.stream().map(s -> normalize(s.name())).collect(Collectors.toSet());
        if (names.contains("loss of consciousness") || (names.contains("chest pain") && names.contains("shortness of breath"))) {
            return new SeverityResult(SymptomAnalysisV2Response.Severity.EMERGENCY, "Emergency red flag symptoms detected.");
        }
        if (names.contains("hemoptysis") || names.contains("coughing blood")) {
            return new SeverityResult(SymptomAnalysisV2Response.Severity.URGENT, "Hemoptysis requires urgent evaluation.");
        }
        if (symptomIds.size() >= 8) {
            return new SeverityResult(SymptomAnalysisV2Response.Severity.MODERATE, "Multiple symptoms reported; clinical review is recommended.");
        }
        return new SeverityResult(SymptomAnalysisV2Response.Severity.NORMAL, "No deterministic red flag rule matched.");
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private static double toDouble(Object value) {
        return value instanceof BigDecimal decimal ? decimal.doubleValue() : ((Number) value).doubleValue();
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(100, value));
    }

    private static double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private static final class DiseaseBucket {
        private final Long id;
        private final String name;
        private final List<WeightedSymptom> symptoms = new ArrayList<>();

        private DiseaseBucket(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    private record WeightedSymptom(Long id, String name, double weight) {
        private SymptomInfo toInfo() {
            return new SymptomInfo(id, name);
        }
    }

    private record SeverityResult(SymptomAnalysisV2Response.Severity severity, String explanation) {}

    private record DiseaseResult(
            Long diseaseId,
            String diseaseName,
            int confidence,
            double matchScore,
            List<SymptomInfo> matchedSymptoms,
            List<SymptomInfo> missingSymptoms,
            List<SymptomInfo> recommendedNextSymptoms,
            List<SymptomInfo> matchedCriticalSymptoms,
            List<SymptomInfo> missingCriticalSymptoms,
            double averageMatchedWeight,
            String clinicalExplanation,
            SeverityResult severity
    ) {
        private SymptomAnalysisV2Response toDto() {
            return SymptomAnalysisV2Response.builder()
                    .diseaseId(diseaseId)
                    .diseaseName(diseaseName)
                    .matchScore(matchScore)
                    .confidence(confidence)
                    .severity(severity.severity())
                    .severityExplanation(severity.explanation())
                    .clinicalExplanation(clinicalExplanation)
                    .matchedSymptoms(matchedSymptoms)
                    .missingSymptoms(missingSymptoms)
                    .recommendedNextSymptoms(recommendedNextSymptoms)
                    .matchedCriticalSymptoms(matchedCriticalSymptoms)
                    .missingCriticalSymptoms(missingCriticalSymptoms)
                    .build();
        }
    }
}
