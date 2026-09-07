package com.duoq.medlearn.knowledge.disease.service;
import com.duoq.medlearn.knowledge.disease.service.impl.DiseaseServiceImpl;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import com.duoq.medlearn.knowledge.category.repository.CategoryRepository;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.category.entity.Category;
import com.duoq.medlearn.knowledge.disease.entity.Disease;

import com.duoq.medlearn.auth.entity.*;
import com.duoq.medlearn.knowledge.disease.dto.request.*;
import com.duoq.medlearn.knowledge.disease.dto.response.*;
import com.duoq.medlearn.knowledge.disease.dto.projection.*;
import com.duoq.medlearn.auth.enums.PermissionCode;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.knowledge.disease.mapper.DiseaseMapper;
import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.audit.service.AuditService;
import com.duoq.medlearn.knowledge.version.service.DiseaseVersionService;
import com.duoq.medlearn.auth.service.PermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiseaseServiceImplTest {

    @Mock private DiseaseRepository diseaseRepository;
    @Mock private DiseaseVersionRepository diseaseVersionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private CurrentUserResolver currentUserResolver;
    @Mock private UserRepository userRepository;
    @Mock private DiseaseVersionService diseaseVersionService;
    @Mock private DiseaseMapper diseaseMapper;
    @Mock private AuditService auditService;
    @Mock private PermissionService permissionService;
    @InjectMocks private DiseaseServiceImpl diseaseService;

    private Disease testDisease;
    private User testUser;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testUser = new User(); testUser.setId(1L); testUser.setUsername("testuser");
        testCategory = new Category(); testCategory.setId(1L); testCategory.setName("Metabolic");
        testDisease = new Disease(); testDisease.setId(1L); testDisease.setName("Diabetes");
        testDisease.setSlug("diabetes"); testDisease.setCategory(testCategory);
        lenient().when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        lenient().when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));
        lenient().when(permissionService.hasPermission(any(PermissionCode.class))).thenReturn(false);
    }

    @Test void createDisease_shouldSucceed() {
        CreateDiseaseRequest req = new CreateDiseaseRequest(); req.setName("New"); req.setSlug("new"); req.setCategoryId(1L);
        when(diseaseRepository.existsByName("New")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(diseaseRepository.save(any(Disease.class))).thenReturn(testDisease);
        when(diseaseMapper.toDiseaseResponse(any(Disease.class))).thenReturn(DiseaseResponse.builder().id(1L).name("New").slug("new").build());
        assertThat(diseaseService.createDisease(req)).isNotNull();
        verify(diseaseVersionService).createDraftVersion(eq(1L), any());
    }

    @Test void createDisease_shouldFail_whenNameExists() {
        CreateDiseaseRequest req = new CreateDiseaseRequest(); req.setName("Diabetes"); req.setSlug("x");
        when(diseaseRepository.existsByName("Diabetes")).thenReturn(true);
        assertThatThrownBy(() -> diseaseService.createDisease(req)).isInstanceOf(IllegalStateException.class).hasMessageContaining("already exists");
    }

    @Test void createDisease_shouldFail_whenSlugExists() {
        CreateDiseaseRequest req = new CreateDiseaseRequest(); req.setName("Unique"); req.setSlug("diabetes");
        when(diseaseRepository.existsByName("Unique")).thenReturn(false);
        when(diseaseRepository.findBySlug("diabetes")).thenReturn(Optional.of(testDisease));
        assertThatThrownBy(() -> diseaseService.createDisease(req)).isInstanceOf(IllegalStateException.class).hasMessageContaining("slug already exists");
    }

    @Test void softDeleteDisease_shouldSucceed() {
        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(testDisease));
        when(diseaseVersionRepository.existsByDiseaseIdAndCreatedByIdAndDeletedAtIsNull(1L, 1L)).thenReturn(true);
        when(diseaseRepository.save(any(Disease.class))).thenReturn(testDisease);
        diseaseService.softDeleteDisease(1L);
        assertThat(testDisease.getDeletedAt()).isNotNull();
        verify(auditService).log(any(), any(), any(), anyLong(), anyMap());
    }

    @Test void softDeleteDisease_shouldFail_whenNotFound() {
        when(diseaseRepository.findById(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> diseaseService.softDeleteDisease(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test void restoreDisease_shouldSucceed() {
        testDisease.setDeletedAt(OffsetDateTime.now().minusHours(1));
        when(diseaseRepository.findByIdIgnoreDeletedAt(1L)).thenReturn(Optional.of(testDisease));
        when(permissionService.hasPermission(PermissionCode.DISEASE_RESTORE)).thenReturn(true);
        when(diseaseRepository.save(any(Disease.class))).thenReturn(testDisease);
        diseaseService.restoreDisease(1L);
        assertThat(testDisease.getDeletedAt()).isNull();
    }

    @Test void restoreDisease_shouldFail_whenNotFound() {
        when(diseaseRepository.findByIdIgnoreDeletedAt(999L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> diseaseService.restoreDisease(999L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test void updateDiseaseMetadata_shouldUpdateNameAndCategory_withoutChangingSlug() {
        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(testDisease));
        when(diseaseVersionRepository.existsByDiseaseIdAndCreatedByIdAndDeletedAtIsNull(1L, 1L)).thenReturn(true);
        when(diseaseRepository.save(any(Disease.class))).thenReturn(testDisease);
        when(diseaseMapper.toDiseaseResponse(any(Disease.class))).thenReturn(
                DiseaseResponse.builder().id(1L).name("Diabetes Renamed").slug("diabetes").build());

        UpdateDiseaseRequest req = new UpdateDiseaseRequest();
        req.setName("Diabetes Renamed");
        req.setSlug("new-slug-should-be-ignored"); // must NOT be applied

        DiseaseResponse result = diseaseService.updateDiseaseMetadata(1L, req);

        assertThat(testDisease.getSlug()).isEqualTo("diabetes"); // slug unchanged
        assertThat(result.getName()).isEqualTo("Diabetes Renamed");
    }

    @Test void updateDiseaseMetadata_shouldFail_whenNameAlreadyExists() {
        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(testDisease));
        when(diseaseVersionRepository.existsByDiseaseIdAndCreatedByIdAndDeletedAtIsNull(1L, 1L)).thenReturn(true);
        when(diseaseRepository.existsByName("Other Disease")).thenReturn(true);

        UpdateDiseaseRequest req = new UpdateDiseaseRequest();
        req.setName("Other Disease");

        assertThatThrownBy(() -> diseaseService.updateDiseaseMetadata(1L, req))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already exists");
    }

    @Test void restoreDisease_shouldFail_whenNoPermission() {
        testDisease.setDeletedAt(OffsetDateTime.now().minusHours(1));
        when(diseaseRepository.findByIdIgnoreDeletedAt(1L)).thenReturn(Optional.of(testDisease));
        when(permissionService.hasPermission(PermissionCode.DISEASE_RESTORE)).thenReturn(false);
        assertThatThrownBy(() -> diseaseService.restoreDisease(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("DISEASE_RESTORE");
    }
}
