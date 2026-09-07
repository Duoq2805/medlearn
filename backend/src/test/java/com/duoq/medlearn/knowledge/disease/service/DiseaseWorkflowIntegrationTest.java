package com.duoq.medlearn.knowledge.disease.service;
import com.duoq.medlearn.knowledge.section.repository.SectionTypeRepository;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.section.entity.SectionType;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.knowledge.disease.entity.Disease;

import com.duoq.medlearn.auth.entity.*;
import com.duoq.medlearn.auth.enums.PermissionCode;
import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import com.duoq.medlearn.knowledge.section.dto.request.CreateDiseaseSectionRequest;
import com.duoq.medlearn.knowledge.version.dto.request.ModerationRequest;
import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;
import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.auth.repository.RoleRepository;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.audit.service.AuditService;
import com.duoq.medlearn.knowledge.section.service.DiseaseSectionService;
import com.duoq.medlearn.knowledge.version.service.DiseaseVersionService;
import com.duoq.medlearn.knowledge.disease.service.DiseaseWorkflowService;
import com.duoq.medlearn.auth.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DiseaseWorkflowIntegrationTest {

    @Autowired
    private DiseaseVersionService diseaseVersionService;

    @Autowired
    private DiseaseSectionService diseaseSectionService;

    @Autowired
    private DiseaseWorkflowService diseaseWorkflowService;

    @Autowired
    private DiseaseRepository diseaseRepository;

    @Autowired
    private DiseaseVersionRepository diseaseVersionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SectionTypeRepository sectionTypeRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockBean
    private CurrentUserResolver currentUserResolver;

    @MockBean
    private AuditService auditService;

    @MockBean
    private PermissionService permissionService;

    private Disease testDisease;
    private User contributor;
    private User reviewer;
    private SectionType definitionType;
    private SectionType symptomsType;
    private SectionType treatmentType;

    @BeforeEach
    void setUp() {
        // Create roles
        Role contributorRole = roleRepository.findByName("CONTRIBUTOR")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("CONTRIBUTOR");
                    role.setDescription("Contributor role");
                    return roleRepository.save(role);
                });

        Role reviewerRole = roleRepository.findByName("REVIEWER")
                .orElseGet(() -> {
                    Role role = new Role();
                    role.setName("REVIEWER");
                    role.setDescription("Reviewer role");
                    return roleRepository.save(role);
                });

        // Create users
        contributor = new User();
        contributor.setUsername("contributor");
        contributor.setEmail("contributor@example.com");
        contributor.setPasswordHash("$2a$10$dummy_hash_for_testing_purposes_only"); // Required non-null
        contributor.setFullName("Test Contributor");
        Set<Role> contributorRoles = new HashSet<>();
        contributorRoles.add(contributorRole);
        contributor.setRoles(contributorRoles);
        contributor = userRepository.save(contributor);

        reviewer = new User();
        reviewer.setUsername("reviewer");
        reviewer.setEmail("reviewer@example.com");
        reviewer.setPasswordHash("$2a$10$dummy_hash_for_testing_purposes_only"); // Required non-null
        reviewer.setFullName("Test Reviewer");
        Set<Role> reviewerRoles = new HashSet<>();
        reviewerRoles.add(reviewerRole);
        reviewer.setRoles(reviewerRoles);
        reviewer = userRepository.save(reviewer);

        // Create section types
        definitionType = sectionTypeRepository.findByName("definition")
                .orElseGet(() -> {
                    SectionType type = new SectionType();
                    type.setName("definition");
                    type.setDescription("Disease definition");
                    return sectionTypeRepository.save(type);
                });

        symptomsType = sectionTypeRepository.findByName("symptoms")
                .orElseGet(() -> {
                    SectionType type = new SectionType();
                    type.setName("symptoms");
                    type.setDescription("Disease symptoms");
                    return sectionTypeRepository.save(type);
                });

        treatmentType = sectionTypeRepository.findByName("treatment")
                .orElseGet(() -> {
                    SectionType type = new SectionType();
                    type.setName("treatment");
                    type.setDescription("Disease treatment");
                    return sectionTypeRepository.save(type);
                });

        // Create disease with draft version
        testDisease = new Disease();
        testDisease.setName("Integration Test Disease");
        testDisease.setSlug("integration-test-disease");
        testDisease = diseaseRepository.save(testDisease);

        DiseaseVersion draftVersion = new DiseaseVersion();
        draftVersion.setDisease(testDisease);
        draftVersion.setVersionNumber(1);
        draftVersion.setStatus(VersionStatus.DRAFT);
        draftVersion.setCreatedBy(contributor);
        draftVersion = diseaseVersionRepository.save(draftVersion);

        testDisease.setCurrentVersion(draftVersion);
        diseaseRepository.save(testDisease);

        // Grant all permissions for test
        when(permissionService.hasPermission(any(PermissionCode.class))).thenReturn(true);
    }

    @Test
    void fullWorkflow_shouldSucceed_whenAllRequiredSectionsPresent() {
        // Given: Mock current user as contributor
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(contributor.getId());

        DiseaseVersion version = testDisease.getCurrentVersion();
        Long versionId = version.getId();

        // Step 1: Add all required sections
        CreateDiseaseSectionRequest definitionRequest = new CreateDiseaseSectionRequest();
        definitionRequest.setSectionTypeId(definitionType.getId());
        definitionRequest.setTitle("Definition");
        definitionRequest.setContent("Test definition content");
        definitionRequest.setOrderIndex(1);
        diseaseSectionService.createSection(versionId, definitionRequest);

        CreateDiseaseSectionRequest symptomsRequest = new CreateDiseaseSectionRequest();
        symptomsRequest.setSectionTypeId(symptomsType.getId());
        symptomsRequest.setTitle("Symptoms");
        symptomsRequest.setContent("Test symptoms content");
        symptomsRequest.setOrderIndex(2);
        diseaseSectionService.createSection(versionId, symptomsRequest);

        CreateDiseaseSectionRequest treatmentRequest = new CreateDiseaseSectionRequest();
        treatmentRequest.setSectionTypeId(treatmentType.getId());
        treatmentRequest.setTitle("Treatment");
        treatmentRequest.setContent("Test treatment content");
        treatmentRequest.setOrderIndex(3);
        diseaseSectionService.createSection(versionId, treatmentRequest);

        // Step 2: Submit for review
        DiseaseVersionResponse submittedVersion = diseaseWorkflowService.submit(versionId);
        assertThat(submittedVersion).isNotNull();
        assertThat(submittedVersion.getStatus()).isEqualTo(VersionStatus.PENDING_REVIEW);

        // Step 3: Approve version (as reviewer)
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(reviewer.getId());
        ModerationRequest moderationRequest = new ModerationRequest();
        moderationRequest.setNote("Approved for publication");

        DiseaseVersionResponse approvedVersion = diseaseWorkflowService.approve(versionId, moderationRequest);
        assertThat(approvedVersion).isNotNull();
        assertThat(approvedVersion.getStatus()).isEqualTo(VersionStatus.APPROVED);
    }

    @Test
    void submitForReview_shouldFail_whenDefinitionSectionMissing() {
        // Given: Mock current user as contributor
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(contributor.getId());

        DiseaseVersion version = testDisease.getCurrentVersion();
        Long versionId = version.getId();

        // Add only symptoms and treatment (missing definition)
        CreateDiseaseSectionRequest symptomsRequest = new CreateDiseaseSectionRequest();
        symptomsRequest.setSectionTypeId(symptomsType.getId());
        symptomsRequest.setTitle("Symptoms");
        symptomsRequest.setContent("Test symptoms");
        diseaseSectionService.createSection(versionId, symptomsRequest);

        CreateDiseaseSectionRequest treatmentRequest = new CreateDiseaseSectionRequest();
        treatmentRequest.setSectionTypeId(treatmentType.getId());
        treatmentRequest.setTitle("Treatment");
        treatmentRequest.setContent("Test treatment");
        diseaseSectionService.createSection(versionId, treatmentRequest);

        // When/Then: Submit should fail
        assertThatThrownBy(() -> diseaseWorkflowService.submit(versionId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot submit version for review")
                .hasMessageContaining("Definition");

        // Verify version remains in DRAFT status
        DiseaseVersion stillDraft = diseaseVersionRepository.findById(versionId).orElseThrow();
        assertThat(stillDraft.getStatus()).isEqualTo(VersionStatus.DRAFT);
    }
}
