package com.duoq.medlearn.knowledge.symptom.service;
import com.duoq.medlearn.knowledge.symptom.repository.SymptomRepository;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionSymptomRepository;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.symptom.entity.Symptom;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersionSymptom;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.knowledge.disease.entity.Disease;

import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomAnalysisV2Response;
import com.duoq.medlearn.knowledge.symptom.service.SymptomCheckerServiceV2;
import com.duoq.medlearn.auth.entity.*;
import com.duoq.medlearn.auth.enums.PermissionCode;
import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import com.duoq.medlearn.audit.service.AuditService;
import com.duoq.medlearn.auth.service.PermissionService;
import com.duoq.medlearn.knowledge.section.service.DiseaseSectionService;
import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.auth.repository.RoleRepository;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SymptomCheckerV2IntegrationTest {

    @Autowired private SymptomCheckerServiceV2 service;
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

    private User user;
    private Symptom fever;
    private Symptom cough;
    private Symptom chestPain;
    private Symptom shortnessOfBreath;

    @BeforeEach
    void setUp() {
        Role role = roleRepository.findByName("USER").orElseGet(() -> {
            Role r = new Role();
            r.setName("USER");
            return roleRepository.save(r);
        });
        user = new User();
        user.setUsername("symptom-v2-user");
        user.setEmail("symptom-v2@example.com");
        user.setPasswordHash("$2a$10$dummy");
        user.setRoles(new HashSet<>(Set.of(role)));
        user = userRepository.save(user);
        fever = symptom("Fever", "v2-fever");
        cough = symptom("Cough", "v2-cough");
        Symptom fatigue = symptom("Fatigue", "v2-fatigue");
        chestPain = symptom("Chest pain", "v2-chest-pain");
        shortnessOfBreath = symptom("Shortness of breath", "v2-shortness-breath");
        disease("Flu V2", "flu-v2", VersionStatus.APPROVED, false, link(fever, 5), link(cough, 4), link(fatigue, 2));
        disease("Draft V2", "draft-v2", VersionStatus.DRAFT, false, link(fever, 5));
        disease("Archived V2", "archived-v2", VersionStatus.ARCHIVED, false, link(fever, 5));
        disease("Rejected V2", "rejected-v2", VersionStatus.REJECTED, false, link(fever, 5));
        disease("Deleted V2", "deleted-v2", VersionStatus.APPROVED, true, link(fever, 5));
        disease("Cardiac V2", "cardiac-v2", VersionStatus.APPROVED, false, link(chestPain, 5), link(shortnessOfBreath, 5));
        when(permissionService.hasPermission(any(PermissionCode.class))).thenReturn(true);
    }

    @Test void analyze_shouldIncludeOnlyApprovedCurrentNonDeletedDisease() {
        List<SymptomAnalysisV2Response> results = service.analyze(List.of(fever.getId(), cough.getId()));
        assertThat(results).extracting(SymptomAnalysisV2Response::getDiseaseName).contains("Flu V2");
        assertThat(results).extracting(SymptomAnalysisV2Response::getDiseaseName)
                .doesNotContain("Draft V2", "Archived V2", "Rejected V2", "Deleted V2");
    }

    @Test void analyze_shouldDetectEmergencyInDatabaseFlow() {
        SymptomAnalysisV2Response result = service.analyze(List.of(chestPain.getId(), shortnessOfBreath.getId())).stream()
                .filter(r -> r.getDiseaseName().equals("Cardiac V2"))
                .findFirst()
                .orElseThrow();
        assertThat(result.getSeverity()).isEqualTo(SymptomAnalysisV2Response.Severity.EMERGENCY);
        assertThat(result.getConfidence()).isEqualTo(100);
    }

    private Symptom symptom(String name, String slug) {
        Symptom s = new Symptom();
        s.setName(name);
        s.setSlug(slug);
        return symptomRepository.save(s);
    }

    private SymptomWeight link(Symptom symptom, int weight) {
        return new SymptomWeight(symptom, weight);
    }

    private void disease(String name, String slug, VersionStatus status, boolean deleted, SymptomWeight... symptoms) {
        Disease d = new Disease();
        d.setName(name);
        d.setSlug(slug);
        if (deleted) d.setDeletedAt(OffsetDateTime.now());
        d = diseaseRepository.save(d);
        DiseaseVersion v = new DiseaseVersion();
        v.setDisease(d);
        v.setVersionNumber(1);
        v.setStatus(status);
        v.setCreatedBy(user);
        v = diseaseVersionRepository.save(v);
        for (SymptomWeight symptom : symptoms) {
            DiseaseVersionSymptom dvs = new DiseaseVersionSymptom();
            dvs.setDiseaseVersion(v);
            dvs.setSymptom(symptom.symptom());
            dvs.setWeightScore(BigDecimal.valueOf(symptom.weight()));
            dvsRepository.save(dvs);
        }
        d.setCurrentVersion(v);
        diseaseRepository.save(d);
    }

    private record SymptomWeight(Symptom symptom, int weight) {}
}
