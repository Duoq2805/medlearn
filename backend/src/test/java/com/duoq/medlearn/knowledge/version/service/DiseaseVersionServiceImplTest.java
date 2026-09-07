package com.duoq.medlearn.knowledge.version.service;
import com.duoq.medlearn.knowledge.version.service.impl.DiseaseVersionServiceImpl;

import com.duoq.medlearn.knowledge.disease.entity.Disease;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.knowledge.disease.mapper.DiseaseMapper;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.auth.enums.PermissionCode;
import com.duoq.medlearn.audit.service.AuditService;
import com.duoq.medlearn.knowledge.section.service.DiseaseSectionService;
import com.duoq.medlearn.auth.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DiseaseVersionServiceImplTest {

    @Mock
    private DiseaseVersionRepository diseaseVersionRepository;

    @Mock
    private DiseaseRepository diseaseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CurrentUserResolver currentUserResolver;

    @Mock
    private DiseaseMapper diseaseMapper;

    @Mock
    private AuditService auditService;

    @Mock
    private DiseaseSectionService diseaseSectionService;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private DiseaseVersionServiceImpl diseaseVersionService;

    private DiseaseVersion draftVersion;
    private User testUser;
    private Disease testDisease;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testDisease = new Disease();
        testDisease.setId(1L);
        testDisease.setName("Test Disease");

        draftVersion = new DiseaseVersion();
        draftVersion.setId(1L);
        draftVersion.setVersionNumber(1);
        draftVersion.setStatus(VersionStatus.DRAFT);
        draftVersion.setDisease(testDisease);
        draftVersion.setCreatedBy(testUser);

        // Mock permission service to return false (allows ownership checks to proceed)
        // Using lenient() to avoid UnnecessaryStubbing errors in tests that don't use it
        lenient().when(permissionService.hasPermission(any(PermissionCode.class))).thenReturn(false);
    }

    @Test
    void submitForReview_shouldPass_whenAllRequiredSectionsExist() {
        // Given
        when(diseaseVersionRepository.findById(1L)).thenReturn(Optional.of(draftVersion));
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));
        when(diseaseVersionRepository.save(any(DiseaseVersion.class))).thenReturn(draftVersion);
        when(diseaseMapper.toDiseaseVersionResponse(any(DiseaseVersion.class)))
                .thenReturn(DiseaseVersionResponse.builder().id(1L).status(VersionStatus.PENDING_REVIEW).build());
        doNothing().when(diseaseSectionService).validateRequiredSections(1L);

        // When
        DiseaseVersionResponse result = diseaseVersionService.submitForReview(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(draftVersion.getStatus()).isEqualTo(VersionStatus.PENDING_REVIEW);
        verify(diseaseSectionService).validateRequiredSections(1L);
        verify(diseaseVersionRepository).save(draftVersion);
        verify(auditService).log(any(), any(), any(), any(), any());
    }

    @Test
    void submitForReview_shouldFail_whenDefinitionMissing() {
        // Given
        when(diseaseVersionRepository.findById(1L)).thenReturn(Optional.of(draftVersion));
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));
        doThrow(new IllegalStateException("Cannot submit version for review.\nMissing required sections:\n- Definition"))
                .when(diseaseSectionService).validateRequiredSections(1L);

        // When/Then
        assertThatThrownBy(() -> diseaseVersionService.submitForReview(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot submit version for review")
                .hasMessageContaining("Definition");

        // Verify version status remains DRAFT
        assertThat(draftVersion.getStatus()).isEqualTo(VersionStatus.DRAFT);
        verify(diseaseVersionRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void submitForReview_shouldFail_whenSymptomsMissing() {
        // Given
        when(diseaseVersionRepository.findById(1L)).thenReturn(Optional.of(draftVersion));
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));
        doThrow(new IllegalStateException("Cannot submit version for review.\nMissing required sections:\n- Symptoms"))
                .when(diseaseSectionService).validateRequiredSections(1L);

        // When/Then
        assertThatThrownBy(() -> diseaseVersionService.submitForReview(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot submit version for review")
                .hasMessageContaining("Symptoms");

        // Verify version status remains DRAFT
        assertThat(draftVersion.getStatus()).isEqualTo(VersionStatus.DRAFT);
        verify(diseaseVersionRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void submitForReview_shouldFail_whenTreatmentMissing() {
        // Given
        when(diseaseVersionRepository.findById(1L)).thenReturn(Optional.of(draftVersion));
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));
        doThrow(new IllegalStateException("Cannot submit version for review.\nMissing required sections:\n- Treatment"))
                .when(diseaseSectionService).validateRequiredSections(1L);

        // When/Then
        assertThatThrownBy(() -> diseaseVersionService.submitForReview(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot submit version for review")
                .hasMessageContaining("Treatment");

        // Verify version status remains DRAFT
        assertThat(draftVersion.getStatus()).isEqualTo(VersionStatus.DRAFT);
        verify(diseaseVersionRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void submitForReview_shouldFail_whenMultipleRequiredSectionsMissing() {
        // Given
        when(diseaseVersionRepository.findById(1L)).thenReturn(Optional.of(draftVersion));
        when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));
        doThrow(new IllegalStateException("Cannot submit version for review.\nMissing required sections:\n- Symptoms\n- Treatment"))
                .when(diseaseSectionService).validateRequiredSections(1L);

        // When/Then
        assertThatThrownBy(() -> diseaseVersionService.submitForReview(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot submit version for review")
                .hasMessageContaining("Symptoms")
                .hasMessageContaining("Treatment");

        // Verify version status remains DRAFT
        assertThat(draftVersion.getStatus()).isEqualTo(VersionStatus.DRAFT);
        verify(diseaseVersionRepository, never()).save(any());
        verify(auditService, never()).log(any(), any(), any(), any(), any());
    }

    @Test
    void submitForReview_shouldFail_whenVersionNotFound() {
        // Given
        when(diseaseVersionRepository.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> diseaseVersionService.submitForReview(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Disease version not found");

        verify(diseaseSectionService, never()).validateRequiredSections(anyLong());
        verify(diseaseVersionRepository, never()).save(any());
    }
}
