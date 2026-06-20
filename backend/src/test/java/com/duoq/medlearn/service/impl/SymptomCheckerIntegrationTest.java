package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.dto.ai.DiseaseMatchResultDTO;
import com.duoq.medlearn.domain.entity.*;
import com.duoq.medlearn.domain.enums.VersionStatus;
import com.duoq.medlearn.repository.*;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.service.SymptomCheckerServiceV1;
import com.duoq.medlearn.service.AuditService;
import com.duoq.medlearn.service.PermissionService;
import com.duoq.medlearn.service.DiseaseSectionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SymptomCheckerIntegrationTest {

    @Autowired private SymptomCheckerServiceV1 symptomCheckerServiceV1;
    @Autowired private DiseaseRepository diseaseRepository;
    @Autowired private DiseaseVersionRepository diseaseVersionRepository;
    @Autowired private DiseaseVersionSymptomRepository dvsRepository;
    @Autowired private SymptomRepository symptomRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    @MockBean private CurrentUserResolver currentUserResolver;
    @MockBean private AuditService auditService;
    @MockBean private PermissionService permissionService;
    @MockBean private DiseaseSectionService diseaseSectionService;

    private Disease approvedDisease;
    private Disease draftDisease;
    private Disease archivedDisease;
    private Symptom fever;
    private Symptom cough;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Create test user
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> {
            Role r = new Role(); r.setName("USER"); r.setDescription("User");
            return roleRepository.save(r);
        });
        testUser = new User();
        testUser.setUsername("test-symptom-user");
        testUser.setEmail("symptom-test@example.com");
        testUser.setPasswordHash("$2a$10$dummy");
        testUser.setRoles(new HashSet<>(Set.of(userRole)));
        testUser = userRepository.save(testUser);

        // Create symptoms
        fever = symptomRepository.save(createSymptom("Fever", "fever"));
        cough = symptomRepository.save(createSymptom("Cough", "cough"));
        Symptom fatigue = symptomRepository.save(createSymptom("Fatigue", "fatigue"));
        Symptom headache = symptomRepository.save(createSymptom("Headache", "headache"));

        // Disease 1: APPROVED with symptoms fever, cough, fatigue (3)
        approvedDisease = diseaseRepository.save(createDisease("Approved Disease", "approved-disease"));
        DiseaseVersion approvedV = createVersion(approvedDisease, VersionStatus.APPROVED);
        linkSymptom(approvedV, fever);
        linkSymptom(approvedV, cough);
        linkSymptom(approvedV, fatigue);
        approvedDisease.setCurrentVersion(approvedV);
        diseaseRepository.save(approvedDisease);

        // Disease 2: DRAFT with symptoms fever, headache (2) — should be excluded
        draftDisease = diseaseRepository.save(createDisease("Draft Disease", "draft-disease"));
        DiseaseVersion draftV = createVersion(draftDisease, VersionStatus.DRAFT);
        draftV.setId(diseaseVersionRepository.save(draftV).getId());
        linkSymptom(draftV, fever);
        linkSymptom(draftV, headache);

        // Disease 3: APPROVED but archived — symptoms fever, cough (2) — should be excluded
        archivedDisease = diseaseRepository.save(createDisease("Archived Disease", "archived-disease"));
        DiseaseVersion archivedV = createVersion(archivedDisease, VersionStatus.ARCHIVED);
        linkSymptom(archivedV, fever);
        linkSymptom(archivedV, cough);
        // Even though currentVersion is set, version is ARCHIVED so symptom link won't resolve

        // Disease 4: APPROVED with no currentVersion set — should be excluded
        Disease noCurrentDisease = diseaseRepository.save(createDisease("No Current", "no-current-disease"));
        DiseaseVersion noCurrV = createVersion(noCurrentDisease, VersionStatus.APPROVED);
        linkSymptom(noCurrV, fever);
        // Intentionally NOT setting currentVersion

        when(permissionService.hasPermission(any())).thenReturn(true);
    }

    @Test
    void analyze_shouldIncludeApprovedDisease() {
        List<Long> userSymptoms = Arrays.asList(fever.getId(), cough.getId());
        List<DiseaseMatchResultDTO> results = symptomCheckerServiceV1.analyze(userSymptoms);
        Optional<DiseaseMatchResultDTO> approved = results.stream()
                .filter(r -> r.getDiseaseName().equals("Approved Disease"))
                .findFirst();
        assertThat(approved).isPresent();
        assertThat(approved.get().getMatchScore()).isGreaterThan(0);
    }

    @Test
    void analyze_shouldExcludeDraftDisease() {
        List<Long> userSymptoms = Arrays.asList(fever.getId(), cough.getId());
        List<DiseaseMatchResultDTO> results = symptomCheckerServiceV1.analyze(userSymptoms);
        Optional<DiseaseMatchResultDTO> draft = results.stream()
                .filter(r -> r.getDiseaseName().equals("Draft Disease"))
                .findFirst();
        assertThat(draft).isEmpty();
    }

    @Test
    void analyze_shouldExcludeArchivedDisease() {
        List<Long> userSymptoms = Arrays.asList(fever.getId(), cough.getId());
        List<DiseaseMatchResultDTO> results = symptomCheckerServiceV1.analyze(userSymptoms);
        Optional<DiseaseMatchResultDTO> archived = results.stream()
                .filter(r -> r.getDiseaseName().equals("Archived Disease"))
                .findFirst();
        assertThat(archived).isEmpty();
    }

    @Test
    void analyze_shouldExcludeDiseaseWithoutCurrentVersion() {
        List<Long> userSymptoms = Arrays.asList(fever.getId(), cough.getId());
        List<DiseaseMatchResultDTO> results = symptomCheckerServiceV1.analyze(userSymptoms);
        Optional<DiseaseMatchResultDTO> noCurrent = results.stream()
                .filter(r -> r.getDiseaseName().equals("No Current"))
                .findFirst();
        assertThat(noCurrent).isEmpty();
    }

    @Test
    void analyze_shouldReturnEmpty_whenUserSymptomsEmpty() {
        List<DiseaseMatchResultDTO> results = symptomCheckerServiceV1.analyze(new ArrayList<>());
        assertThat(results).isEmpty();
    }

    // ============ HELPERS ============

    private Symptom createSymptom(String name, String slug) {
        Symptom s = new Symptom();
        s.setName(name);
        s.setSlug(slug);
        return s;
    }

    private Disease createDisease(String name, String slug) {
        Disease d = new Disease();
        d.setName(name);
        d.setSlug(slug);
        return d;
    }

    private DiseaseVersion createVersion(Disease disease, VersionStatus status) {
        DiseaseVersion v = new DiseaseVersion();
        v.setDisease(disease);
        v.setVersionNumber(1);
        v.setStatus(status);
        v.setCreatedBy(testUser);
        v = diseaseVersionRepository.save(v);
        return v;
    }

    private void linkSymptom(DiseaseVersion version, Symptom symptom) {
        DiseaseVersionSymptom dvs = new DiseaseVersionSymptom();
        dvs.setDiseaseVersion(version);
        dvs.setSymptom(symptom);
        dvsRepository.save(dvs);
    }
}
