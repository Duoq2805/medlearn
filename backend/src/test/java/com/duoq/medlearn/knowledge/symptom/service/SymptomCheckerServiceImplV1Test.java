package com.duoq.medlearn.knowledge.symptom.service;

import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomAnalysisResponse;
import com.duoq.medlearn.knowledge.symptom.service.impl.SymptomCheckerServiceImplV1;
import com.duoq.medlearn.knowledge.symptom.entity.Symptom;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionSymptomRepository;
import com.duoq.medlearn.knowledge.symptom.repository.SymptomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SymptomCheckerServiceImplV1Test {

    @Mock private DiseaseVersionSymptomRepository dvsRepository;
    @Mock private DiseaseRepository diseaseRepository;
    @Mock private SymptomRepository symptomRepository;
    @InjectMocks private SymptomCheckerServiceImplV1 service;

    private List<Long> userSymptomIds;

    @BeforeEach
    void setUp() {
        userSymptomIds = Arrays.asList(1L, 2L);
    }

    @Test void analyze_shouldReturnEmpty_whenInputIsNull() {
        assertThat(service.analyze(null)).isEmpty();
    }

    @Test void analyze_shouldReturnEmpty_whenInputIsEmpty() {
        assertThat(service.analyze(Collections.emptyList())).isEmpty();
    }

    @Test void analyze_shouldReturnExactMatch() {
        List<Long> diseaseIds = Arrays.asList(1L);
        List<Object[]> raw = new ArrayList<>(); raw.add(row(1L, "Flu", 2L, 2));
        when(dvsRepository.findMatchingDiseasesRaw(userSymptomIds, 100)).thenReturn(raw);
        List<Object[]> counts = new ArrayList<>(); counts.add(countRow(1L, 2L));
        when(dvsRepository.findTotalSymptomCountBatch(diseaseIds)).thenReturn(counts);
        List<Object[]> mappings = new ArrayList<>(); mappings.add(mappingRow(1L, 1L)); mappings.add(mappingRow(1L, 2L));
        when(dvsRepository.findDiseaseSymptomMappings(diseaseIds)).thenReturn(mappings);
        // Both user symptoms AND all disease symptoms need name resolution
        when(symptomRepository.findAllByIdIn(anyList())).thenReturn(Arrays.asList(symptom(1L, "Fever"), symptom(2L, "Cough")));

        List<SymptomAnalysisResponse> results = service.analyze(userSymptomIds);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getDiseaseName()).isEqualTo("Flu");
        assertThat(results.get(0).getMatchScore()).isEqualTo(100.0);
        assertThat(results.get(0).getMatchedSymptoms()).hasSize(2);
        assertThat(results.get(0).getMissingSymptoms()).isEmpty();
        assertThat(results.get(0).getExplanation()).isEqualTo("Matched 2 of 2 known symptoms");
    }

    @Test void analyze_shouldReturnPartialMatch() {
        List<Long> diseaseIds = Arrays.asList(1L);
        List<Object[]> raw = new ArrayList<>(); raw.add(row(1L, "Flu", 1L, 1));
        when(dvsRepository.findMatchingDiseasesRaw(userSymptomIds, 100)).thenReturn(raw);
        List<Object[]> counts = new ArrayList<>(); counts.add(countRow(1L, 4L));
        when(dvsRepository.findTotalSymptomCountBatch(diseaseIds)).thenReturn(counts);
        List<Object[]> mappings = new ArrayList<>();
        mappings.add(mappingRow(1L, 1L)); mappings.add(mappingRow(1L, 3L));
        mappings.add(mappingRow(1L, 4L)); mappings.add(mappingRow(1L, 5L));
        when(dvsRepository.findDiseaseSymptomMappings(diseaseIds)).thenReturn(mappings);
        // All symptoms: user ones + disease-only ones
        when(symptomRepository.findAllByIdIn(anyList()))
                .thenReturn(Arrays.asList(
                        symptom(1L, "Fever"),
                        symptom(3L, "Fatigue"),
                        symptom(4L, "Headache"),
                        symptom(5L, "Chills")
                ));

        List<SymptomAnalysisResponse> results = service.analyze(userSymptomIds);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getMatchScore()).isEqualTo(25.0);
        assertThat(results.get(0).getMatchedSymptoms()).hasSize(1);
        assertThat(results.get(0).getMatchedSymptoms().get(0).getName()).isEqualTo("Fever");
        assertThat(results.get(0).getMissingSymptoms()).hasSize(3);
        assertThat(results.get(0).getExplanation()).isEqualTo("Matched 1 of 4 known symptoms");
    }

    @Test void analyze_shouldReturnEmpty_whenNoMatch() {
        when(dvsRepository.findMatchingDiseasesRaw(userSymptomIds, 100)).thenReturn(new ArrayList<>());
        assertThat(service.analyze(userSymptomIds)).isEmpty();
    }

    @Test void analyze_shouldFilterBelowThreshold() {
        List<Long> diseaseIds = Arrays.asList(1L);
        List<Object[]> raw = new ArrayList<>(); raw.add(row(1L, "Low Match", 1L, 1));
        when(dvsRepository.findMatchingDiseasesRaw(userSymptomIds, 100)).thenReturn(raw);
        List<Object[]> counts = new ArrayList<>(); counts.add(countRow(1L, 6L));
        when(dvsRepository.findTotalSymptomCountBatch(diseaseIds)).thenReturn(counts);
        List<Object[]> mappings = new ArrayList<>();
        for (long i = 1; i <= 6; i++) mappings.add(mappingRow(1L, i));
        when(dvsRepository.findDiseaseSymptomMappings(diseaseIds)).thenReturn(mappings);
        when(symptomRepository.findAllByIdIn(anyList()))
                .thenReturn(Arrays.asList(symptom(1L, "Fever"), symptom(2L, "Cough"),
                        symptom(3L, "F3"), symptom(4L, "F4"), symptom(5L, "F5"), symptom(6L, "F6")));

        List<SymptomAnalysisResponse> results = service.analyze(userSymptomIds);
        assertThat(results).isEmpty();
    }

    @Test void analyze_shouldSortByScoreDescending() {
        List<Long> diseaseIds = Arrays.asList(1L, 2L);
        List<Object[]> raw = new ArrayList<>(); raw.add(row(1L, "Disease A", 1L, 1)); raw.add(row(2L, "Disease B", 2L, 2));
        when(dvsRepository.findMatchingDiseasesRaw(userSymptomIds, 100)).thenReturn(raw);
        List<Object[]> counts = new ArrayList<>(); counts.add(countRow(1L, 2L)); counts.add(countRow(2L, 2L));
        when(dvsRepository.findTotalSymptomCountBatch(diseaseIds)).thenReturn(counts);
        List<Object[]> mappings = new ArrayList<>();
        mappings.add(mappingRow(1L, 1L)); mappings.add(mappingRow(1L, 3L));
        mappings.add(mappingRow(2L, 1L)); mappings.add(mappingRow(2L, 2L));
        when(dvsRepository.findDiseaseSymptomMappings(diseaseIds)).thenReturn(mappings);
        when(symptomRepository.findAllByIdIn(anyList()))
                .thenReturn(Arrays.asList(symptom(1L, "Fever"), symptom(2L, "Cough"), symptom(3L, "Fatigue")));

        List<SymptomAnalysisResponse> results = service.analyze(userSymptomIds);

        assertThat(results).hasSize(2);
        assertThat(results.get(0).getDiseaseName()).isEqualTo("Disease B");
        assertThat(results.get(1).getDiseaseName()).isEqualTo("Disease A");
        assertThat(results.get(0).getMatchScore()).isGreaterThan(results.get(1).getMatchScore());
    }

    @Test void analyze_shouldLimitToTop10() {
        List<Object[]> raw = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        for (long i = 1; i <= 15; i++) { raw.add(row(i, "Disease " + i, (long) i, 1)); ids.add(i); }
        when(dvsRepository.findMatchingDiseasesRaw(userSymptomIds, 100)).thenReturn(raw);

        List<Object[]> counts = new ArrayList<>();
        for (Long id : ids) counts.add(countRow(id, 2L));
        when(dvsRepository.findTotalSymptomCountBatch(anyList())).thenReturn(counts);

        List<Object[]> mappings = new ArrayList<>();
        for (Long id : ids) { mappings.add(mappingRow(id, 1L)); mappings.add(mappingRow(id, id + 100L)); }
        when(dvsRepository.findDiseaseSymptomMappings(anyList())).thenReturn(mappings);

        // Mock findAllByIdIn to return all symptoms
        List<Symptom> allSymptoms = new ArrayList<>();
        allSymptoms.add(symptom(1L, "Fever"));
        allSymptoms.add(symptom(2L, "Cough"));
        for (Long id : ids) { allSymptoms.add(symptom(id + 100L, "DiseaseSymptom" + id)); }
        when(symptomRepository.findAllByIdIn(anyList())).thenReturn(allSymptoms);

        List<SymptomAnalysisResponse> results = service.analyze(userSymptomIds);
        assertThat(results).hasSize(10);
    }

    private Object[] row(Long diseaseId, String name, Long totalWeight, int matchCount) {
        return new Object[]{diseaseId, name, "slug-" + diseaseId, matchCount, (double) totalWeight};
    }
    private Object[] countRow(Long diseaseId, Long totalCount) {
        return new Object[]{diseaseId, totalCount};
    }
    private Object[] mappingRow(Long diseaseId, Long symptomId) {
        return new Object[]{diseaseId, symptomId};
    }
    private Symptom symptom(Long id, String name) {
        Symptom s = new Symptom(); s.setId(id); s.setName(name); s.setSlug(name.toLowerCase()); return s;
    }
}
