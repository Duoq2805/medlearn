package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.symptom.dto.response.SymptomAnalysisV2Response;
import com.duoq.medlearn.symptom.service.impl.SymptomCheckerServiceImplV2;
import com.duoq.medlearn.repository.DiseaseVersionSymptomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomCheckerServiceImplV2Test {

    @Mock private DiseaseVersionSymptomRepository dvsRepository;
    @InjectMocks private SymptomCheckerServiceImplV2 service;

    @Test void analyze_shouldReturnEmpty_whenInputNullEmptyOrNoMatch() {
        assertThat(service.analyze(null)).isEmpty();
        assertThat(service.analyze(List.of())).isEmpty();
        when(dvsRepository.findMatchingDiseasesRaw(List.of(1L), 100)).thenReturn(List.of());
        assertThat(service.analyze(List.of(1L))).isEmpty();
    }

    @Test void analyze_shouldCalculateExactMatchConfidenceAndExplanation() {
        stubCandidates(row(1L, "Flu", 2));
        when(dvsRepository.findDiseaseSymptomDetailsForV2(List.of(1L))).thenReturn(List.of(
                detail(1L, "Flu", 1L, "Fever", 5),
                detail(1L, "Flu", 2L, "Cough", 4)
        ));

        List<SymptomAnalysisV2Response> results = service.analyze(List.of(1L, 2L));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getConfidence()).isEqualTo(100);
        assertThat(results.get(0).getMatchScore()).isEqualTo(100.0);
        assertThat(results.get(0).getMatchedCriticalSymptoms()).extracting("name").containsExactly("Fever", "Cough");
        assertThat(results.get(0).getMissingCriticalSymptoms()).isEmpty();
        assertThat(results.get(0).getClinicalExplanation()).contains("Patient matches 2 of 2 characteristic symptoms");
    }

    @Test void analyze_shouldReturnPartialMatchRecommendationsAndThreshold() {
        stubCandidates(row(1L, "Flu", 1));
        when(dvsRepository.findDiseaseSymptomDetailsForV2(List.of(1L))).thenReturn(List.of(
                detail(1L, "Flu", 1L, "Fever", 5),
                detail(1L, "Flu", 2L, "Cough", 4),
                detail(1L, "Flu", 3L, "Fatigue", 3),
                detail(1L, "Flu", 4L, "Headache", 1)
        ));

        List<SymptomAnalysisV2Response> results = service.analyze(List.of(1L));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getConfidence()).isEqualTo(54);
        assertThat(results.get(0).getRecommendedNextSymptoms()).extracting("name").containsExactly("Cough", "Fatigue", "Headache");
        assertThat(results.get(0).getMissingCriticalSymptoms()).extracting("name").containsExactly("Cough");
    }

    @Test void analyze_shouldFilterBelowThreshold() {
        stubCandidates(row(1L, "Low", 1));
        when(dvsRepository.findDiseaseSymptomDetailsForV2(List.of(1L))).thenReturn(List.of(
                detail(1L, "Low", 1L, "Fever", 1),
                detail(1L, "Low", 2L, "A", 10),
                detail(1L, "Low", 3L, "B", 10),
                detail(1L, "Low", 4L, "C", 10),
                detail(1L, "Low", 5L, "D", 10)
        ));

        assertThat(service.analyze(List.of(1L))).isEmpty();
    }

    @Test void analyze_shouldSortByConfidenceMatchedAverageWeightAndName() {
        stubCandidates(row(1L, "Beta", 2), row(2L, "Alpha", 2), row(3L, "Gamma", 1));
        when(dvsRepository.findDiseaseSymptomDetailsForV2(List.of(1L, 2L, 3L))).thenReturn(List.of(
                detail(1L, "Beta", 1L, "Fever", 3), detail(1L, "Beta", 2L, "Cough", 3),
                detail(2L, "Alpha", 1L, "Fever", 3), detail(2L, "Alpha", 2L, "Cough", 3),
                detail(3L, "Gamma", 1L, "Fever", 5), detail(3L, "Gamma", 4L, "Rash", 5)
        ));

        List<SymptomAnalysisV2Response> results = service.analyze(List.of(1L, 2L, 1L));

        assertThat(results).extracting(SymptomAnalysisV2Response::getDiseaseName).containsExactly("Gamma", "Alpha", "Beta");
    }

    @Test void analyze_shouldDetectRedFlags() {
        stubCandidates(row(1L, "Heart Attack", 2));
        when(dvsRepository.findDiseaseSymptomDetailsForV2(List.of(1L))).thenReturn(List.of(
                detail(1L, "Heart Attack", 1L, "Chest pain", 5),
                detail(1L, "Heart Attack", 2L, "Shortness of breath", 5)
        ));

        SymptomAnalysisV2Response result = service.analyze(List.of(1L, 2L)).get(0);

        assertThat(result.getSeverity()).isEqualTo(SymptomAnalysisV2Response.Severity.EMERGENCY);
        assertThat(result.getSeverityExplanation()).contains("Emergency");
    }

    @Test void analyze_shouldUseOnlyTwoQueriesForLargeDedupedRequest() {
        stubCandidates(row(1L, "Flu", 1));
        when(dvsRepository.findDiseaseSymptomDetailsForV2(List.of(1L))).thenReturn(Collections.singletonList(detail(1L, "Flu", 1L, "Fever", 5)));

        List<Long> symptoms = new ArrayList<>();
        for (long i = 1; i <= 100; i++) symptoms.add(i == 100 ? 1L : i);
        service.analyze(symptoms);

        verify(dvsRepository).findMatchingDiseasesRaw(anyList(), eq(100));
        verify(dvsRepository).findDiseaseSymptomDetailsForV2(List.of(1L));
        verifyNoMoreInteractions(dvsRepository);
    }

    private void stubCandidates(Object[]... rows) {
        when(dvsRepository.findMatchingDiseasesRaw(anyList(), eq(100))).thenReturn(Arrays.asList(rows));
    }

    private Object[] row(Long diseaseId, String name, int matchCount) {
        return new Object[]{diseaseId, name, "slug-" + diseaseId, matchCount, BigDecimal.ONE};
    }

    private Object[] detail(Long diseaseId, String diseaseName, Long symptomId, String symptomName, int weight) {
        return new Object[]{diseaseId, diseaseName, symptomId, symptomName, BigDecimal.valueOf(weight)};
    }
}
