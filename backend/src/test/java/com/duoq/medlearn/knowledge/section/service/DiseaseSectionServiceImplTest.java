package com.duoq.medlearn.knowledge.section.service;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.knowledge.disease.entity.DiseaseSection;
import com.duoq.medlearn.knowledge.section.service.impl.DiseaseSectionServiceImpl;
import com.duoq.medlearn.knowledge.section.repository.SectionTypeRepository;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;
import com.duoq.medlearn.knowledge.section.entity.SectionType;

import com.duoq.medlearn.auth.entity.*;
import com.duoq.medlearn.knowledge.section.dto.request.*;
import com.duoq.medlearn.knowledge.section.dto.response.*;
import com.duoq.medlearn.auth.enums.PermissionCode;
import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.knowledge.disease.mapper.DiseaseMapper;
import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.auth.service.PermissionService;
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
class DiseaseSectionServiceImplTest {

    @Mock private DiseaseSectionRepository diseaseSectionRepository;
    @Mock private DiseaseVersionRepository diseaseVersionRepository;
    @Mock private SectionTypeRepository sectionTypeRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentUserResolver currentUserResolver;
    @Mock private DiseaseMapper diseaseMapper;
    @Mock private PermissionService permissionService;
    @InjectMocks private DiseaseSectionServiceImpl diseaseSectionService;

    private DiseaseSection testSection;
    private DiseaseVersion draftVersion;
    private DiseaseVersion approvedVersion;
    private User testUser;
    private SectionType testSectionType;

    @BeforeEach
    void setUp() {
        testUser = new User(); testUser.setId(1L); testUser.setUsername("testuser");
        testSectionType = new SectionType(); testSectionType.setId(1); testSectionType.setName("Definition");

        draftVersion = new DiseaseVersion(); draftVersion.setId(1L); draftVersion.setStatus(VersionStatus.DRAFT); draftVersion.setCreatedBy(testUser);
        approvedVersion = new DiseaseVersion(); approvedVersion.setId(2L); approvedVersion.setStatus(VersionStatus.APPROVED);

        testSection = new DiseaseSection(); testSection.setId(1L);
        testSection.setDiseaseVersion(draftVersion); testSection.setSectionType(testSectionType);
        testSection.setTitle("Test"); testSection.setContent("content"); testSection.setOrderIndex(0);

        lenient().when(currentUserResolver.resolveCurrentUserId()).thenReturn(1L);
        lenient().when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(testUser));
        lenient().when(permissionService.hasPermission(any(PermissionCode.class))).thenReturn(false);
    }

    @Test void createSection_shouldSucceed() {
        CreateDiseaseSectionRequest req = new CreateDiseaseSectionRequest();
        req.setSectionTypeId(1); req.setTitle("New"); req.setContent("content");
        when(diseaseVersionRepository.findById(1L)).thenReturn(Optional.of(draftVersion));
        when(sectionTypeRepository.findById(1)).thenReturn(Optional.of(testSectionType));
        when(diseaseSectionRepository.save(any(DiseaseSection.class))).thenReturn(testSection);
        when(diseaseMapper.toDiseaseSectionResponse(any(DiseaseSection.class))).thenReturn(DiseaseSectionResponse.builder().id(1L).build());
        assertThat(diseaseSectionService.createSection(1L, req)).isNotNull();
    }

    @Test void createSection_shouldFail_whenVersionNotDraft() {
        when(diseaseVersionRepository.findById(2L)).thenReturn(Optional.of(approvedVersion));
        assertThatThrownBy(() -> diseaseSectionService.createSection(2L, new CreateDiseaseSectionRequest()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Section is not editable");
    }

    @Test void createSection_shouldFail_whenSectionTypeNotFound() {
        when(diseaseVersionRepository.findById(1L)).thenReturn(Optional.of(draftVersion));
        assertThatThrownBy(() -> diseaseSectionService.createSection(1L, new CreateDiseaseSectionRequest()))
                .isInstanceOf(Exception.class);
    }

    @Test void updateSection_shouldSucceed() {
        when(diseaseSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(diseaseSectionRepository.save(any(DiseaseSection.class))).thenReturn(testSection);
        when(diseaseMapper.toDiseaseSectionResponse(any(DiseaseSection.class))).thenReturn(DiseaseSectionResponse.builder().id(1L).build());
        UpdateDiseaseSectionRequest req = new UpdateDiseaseSectionRequest(); req.setTitle("Updated");
        assertThat(diseaseSectionService.updateSection(1L, req)).isNotNull();
    }

    @Test void updateSection_shouldFail_whenVersionNotDraft() {
        testSection.setDiseaseVersion(approvedVersion);
        when(diseaseSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        assertThatThrownBy(() -> diseaseSectionService.updateSection(1L, new UpdateDiseaseSectionRequest()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Section is not editable");
    }

    @Test void deleteSection_shouldSucceed() {
        when(diseaseSectionRepository.findById(1L)).thenReturn(Optional.of(testSection));
        when(diseaseSectionRepository.save(any(DiseaseSection.class))).thenReturn(testSection);
        diseaseSectionService.deleteSection(1L);
        assertThat(testSection.getDeletedAt()).isNotNull();
    }

    @Test void reorderSections_shouldSucceed() {
        DiseaseSection s1 = new DiseaseSection(); s1.setId(1L); s1.setDiseaseVersion(draftVersion); s1.setOrderIndex(0);
        DiseaseSection s2 = new DiseaseSection(); s2.setId(2L); s2.setDiseaseVersion(draftVersion); s2.setOrderIndex(1);
        when(diseaseVersionRepository.findById(1L)).thenReturn(Optional.of(draftVersion));
        when(diseaseSectionRepository.findById(1L)).thenReturn(Optional.of(s1));
        when(diseaseSectionRepository.findById(2L)).thenReturn(Optional.of(s2));
        when(diseaseSectionRepository.save(any(DiseaseSection.class))).thenAnswer(i -> i.getArgument(0));
        SectionOrderRequest r1 = new SectionOrderRequest(); r1.setSectionId(1L); r1.setOrderIndex(2);
        SectionOrderRequest r2 = new SectionOrderRequest(); r2.setSectionId(2L); r2.setOrderIndex(0);
        diseaseSectionService.reorderSections(1L, Arrays.asList(r1, r2));
        assertThat(s1.getOrderIndex()).isEqualTo(2);
        assertThat(s2.getOrderIndex()).isEqualTo(0);
    }

    @Test void reorderSections_shouldFail_whenVersionNotDraft() {
        when(diseaseVersionRepository.findById(2L)).thenReturn(Optional.of(approvedVersion));
        assertThatThrownBy(() -> diseaseSectionService.reorderSections(2L, new ArrayList<>()))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("Section is not editable");
    }

    @Test void validateRequiredSections_shouldSucceed_whenAllPresent() {
        when(diseaseSectionRepository.findAllByVersionIdWithType(1L)).thenReturn(Arrays.asList(
                secOfType("Definition"), secOfType("Symptoms"), secOfType("Treatment")));
        diseaseSectionService.validateRequiredSections(1L);
    }

    @Test void validateRequiredSections_shouldFail_whenMissing() {
        when(diseaseSectionRepository.findAllByVersionIdWithType(1L)).thenReturn(Arrays.asList(secOfType("Symptoms"), secOfType("Treatment")));
        assertThatThrownBy(() -> diseaseSectionService.validateRequiredSections(1L)).isInstanceOf(IllegalStateException.class).hasMessageContaining("Definition");
    }

    private DiseaseSection secOfType(String name) {
        SectionType st = new SectionType(); st.setName(name);
        DiseaseSection ds = new DiseaseSection(); ds.setSectionType(st); return ds;
    }
}
