package com.duoq.medlearn.draft.service;

import com.duoq.medlearn.draft.dto.response.DiseaseDraftResponse;
import com.duoq.medlearn.draft.dto.response.DiseaseDraftSectionResponse;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.entity.DiseaseDraftSection;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftSectionType;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.draft.mapper.DraftMapper;
import com.duoq.medlearn.draft.repository.DiseaseDraftRepository;
import com.duoq.medlearn.draft.repository.DiseaseDraftSectionRepository;
import com.duoq.medlearn.draft.service.impl.DraftLifecycleServiceImpl;
import com.duoq.medlearn.knowledge.disease.entity.Disease;
import com.duoq.medlearn.knowledge.disease.entity.DiseaseSection;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;
import com.duoq.medlearn.knowledge.section.repository.SectionTypeRepository;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import com.duoq.medlearn.audit.service.AuditService;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DraftLifecycleServiceTest {

    @Mock private DiseaseDraftRepository draftRepository;
    @Mock private DiseaseDraftSectionRepository sectionRepository;
    @Mock private DraftMapper draftMapper;
    @Mock private DiseaseRepository diseaseRepository;
    @Mock private DiseaseVersionRepository diseaseVersionRepository;
    @Mock private DiseaseSectionRepository diseaseSectionRepository;
    @Mock private SectionTypeRepository sectionTypeRepository;
    @Mock private UserRepository userRepository;
    @Mock private CurrentUserResolver currentUserResolver;
    @Mock private AuditService auditService;
    @InjectMocks private DraftLifecycleServiceImpl lifecycleService;

    private final Long userId = 1L;
    private final Long reviewerId = 2L;
    private DiseaseDraft draft;
    private DiseaseDraftResponse draftResponse;
    private DiseaseDraftSection section;

    @BeforeEach
    void setUp() {
        draft = DiseaseDraft.builder()
                .id(1L)
                .title("Test Draft")
                .status(DraftStatus.DRAFT)
                .sourceMethod(DraftMethod.MANUAL)
                .createdBy(User.builder().id(userId).build())
                .build();

        section = DiseaseDraftSection.builder()
                .id(1L).draft(draft)
                .sectionType(DraftSectionType.DEFINITION)
                .title("Định nghĩa").content("Nội dung")
                .orderIndex(0).build();

        draftResponse = DiseaseDraftResponse.builder()
                .id(1L).title("Test Draft").status(DraftStatus.DRAFT)
                .sourceMethod(DraftMethod.MANUAL).createdBy(userId)
                .sections(List.of()).build();
    }

    private void stubFindAndBuild() {
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(section));
        when(draftMapper.toResponse(draft)).thenReturn(draftResponse.toBuilder().sections(null).build());
        when(draftMapper.toSectionResponseList(anyList()))
                .thenReturn(List.of(DiseaseDraftSectionResponse.builder().id(1L).build()));
    }

    @Test
    void submitForReview_shouldChangeStatus() {
        stubFindAndBuild();

        var result = lifecycleService.submitForReview(1L, userId);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.PENDING_REVIEW);
        verify(draftRepository).save(draft);
    }

    @Test
    void submitForReview_shouldThrow_whenNotDraftOrRejected() {
        draft.setStatus(DraftStatus.PENDING_REVIEW);
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> lifecycleService.submitForReview(1L, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only DRAFT or REJECTED drafts");
    }

    @Test
    void approve_shouldSucceed() {
        draft.setStatus(DraftStatus.PENDING_REVIEW);
        stubFindAndBuild();

        var result = lifecycleService.approve(1L, reviewerId, "Looks good");

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.APPROVED);
        assertThat(draft.getReviewedBy().getId()).isEqualTo(reviewerId);
        assertThat(draft.getReviewNote()).isEqualTo("Looks good");
        assertThat(draft.getReviewedAt()).isNotNull();
        verify(draftRepository).save(draft);
    }

    @Test
    void approve_shouldThrow_whenNotPendingReview() {
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> lifecycleService.approve(1L, reviewerId, "ok"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING_REVIEW drafts");
    }

    @Test
    void reject_shouldSucceed() {
        draft.setStatus(DraftStatus.PENDING_REVIEW);
        stubFindAndBuild();

        var result = lifecycleService.reject(1L, reviewerId, "Needs more detail");

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.REJECTED);
        assertThat(draft.getReviewedBy().getId()).isEqualTo(reviewerId);
        assertThat(draft.getReviewNote()).isEqualTo("Needs more detail");
        verify(draftRepository).save(draft);
    }

    @Test
    void reject_shouldThrow_whenNotPendingReview() {
        draft.setStatus(DraftStatus.APPROVED);
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> lifecycleService.reject(1L, reviewerId, "no"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only PENDING_REVIEW drafts");
    }

    @Test
    void archive_shouldSucceed() {
        stubFindAndBuild();

        var result = lifecycleService.archive(1L, userId);

        assertThat(draft.getStatus()).isEqualTo(DraftStatus.ARCHIVED);
        verify(draftRepository).save(draft);
    }

    @Test
    void cloneDraft_shouldCreateCopy() {
        var sourceDisease = new Disease();
        sourceDisease.setId(1L);
        draft.setDisease(sourceDisease);

        var savedClone = DiseaseDraft.builder()
                .id(2L).title("Test Draft (Copy)").status(DraftStatus.DRAFT)
                .sourceMethod(DraftMethod.MANUAL).disease(sourceDisease)
                .createdBy(User.builder().id(userId).build()).build();

        var sectionCopy = section.toBuilder().id(null).draft(savedClone).build();

        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(section));
        when(draftRepository.save(any(DiseaseDraft.class))).thenReturn(savedClone);
        when(sectionRepository.save(any(DiseaseDraftSection.class))).thenReturn(sectionCopy);
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(2L))
                .thenReturn(List.of(sectionCopy));

        var cloneResponse = DiseaseDraftResponse.builder()
                .id(2L).title("Test Draft (Copy)").status(DraftStatus.DRAFT)
                .sourceMethod(DraftMethod.MANUAL).createdBy(userId).build();
        when(draftMapper.toResponse(savedClone)).thenReturn(cloneResponse.toBuilder().sections(null).build());
        when(draftMapper.toSectionResponseList(anyList())).thenReturn(List.of());

        var result = lifecycleService.cloneDraft(1L, userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(2L);
        verify(draftRepository, times(1)).save(any(DiseaseDraft.class));
        verify(sectionRepository).save(any(DiseaseDraftSection.class));
    }

    @Test
    void applyToDisease_shouldCreateVersionFromApprovedDraft() {
        var sourceDisease = new Disease();
        sourceDisease.setId(1L);
        draft.setDisease(sourceDisease);
        draft.setStatus(DraftStatus.APPROVED);

        var savedVersion = DiseaseVersion.builder()
                .id(10L)
                .disease(sourceDisease)
                .versionNumber(1)
                .status(com.duoq.medlearn.knowledge.version.enums.VersionStatus.APPROVED)
                .build();

        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(diseaseVersionRepository.findMaxVersionNumberByDiseaseId(1L)).thenReturn(Optional.empty());
        when(diseaseVersionRepository.save(any(DiseaseVersion.class))).thenReturn(savedVersion);
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(section));
        when(sectionTypeRepository.findByName("definition"))
                .thenReturn(Optional.of(com.duoq.medlearn.knowledge.section.entity.SectionType.builder()
                        .id(1).name("definition").build()));
        when(diseaseSectionRepository.save(any(DiseaseSection.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(diseaseRepository.save(any(Disease.class))).thenReturn(sourceDisease);

        lifecycleService.applyToDisease(1L, userId);

        // Version created + currentVersion set + old approved archived + draft archived
        verify(diseaseVersionRepository).save(any(DiseaseVersion.class));
        verify(draftRepository).save(draft);
        assertThat(draft.getStatus()).isEqualTo(DraftStatus.ARCHIVED);
        assertThat(sourceDisease.getCurrentVersion()).isNotNull();
    }

    @Test
    void applyToDisease_shouldThrow_whenNotApproved() {
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> lifecycleService.applyToDisease(1L, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only APPROVED drafts");
    }

    @Test
    void findDraft_shouldThrow_whenNotFound() {
        when(draftRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lifecycleService.submitForReview(999L, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ── NEW TESTS for Draft→Version mapping ──────────────────────────────────

    @Test
    void applyToDisease_shouldThrow_whenNoDiseaseAssociated() {
        // draft.disease == null → must throw before reaching user/section load
        draft.setStatus(DraftStatus.APPROVED);
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> lifecycleService.applyToDisease(1L, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no associated disease");
    }

    @Test
    void applyToDisease_shouldThrow_whenSectionsEmpty() {
        var disease = new Disease(); disease.setId(1L);
        draft.setDisease(disease);
        draft.setStatus(DraftStatus.APPROVED);
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> lifecycleService.applyToDisease(1L, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no sections");
    }

    @Test
    void applyToDisease_shouldThrow_whenSectionTypeUnmapped() {
        // DraftSectionType.EPIDEMIOLOGY has no SectionType in DB → must throw
        var epidSection = DiseaseDraftSection.builder()
                .id(2L).draft(draft)
                .sectionType(DraftSectionType.EPIDEMIOLOGY)
                .title("Dịch tễ").content("Nội dung dịch tễ")
                .orderIndex(0).build();

        var disease = new Disease(); disease.setId(1L);
        draft.setDisease(disease);
        draft.setStatus(DraftStatus.APPROVED);

        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(epidSection));
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(diseaseVersionRepository.findMaxVersionNumberByDiseaseId(1L)).thenReturn(Optional.empty());
        when(diseaseVersionRepository.save(any(DiseaseVersion.class))).thenReturn(
                DiseaseVersion.builder().id(10L).disease(disease).versionNumber(1)
                        .status(com.duoq.medlearn.knowledge.version.enums.VersionStatus.APPROVED).build());
        // simulate section_type not seeded for 'epidemiology'
        when(sectionTypeRepository.findByName("epidemiology")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> lifecycleService.applyToDisease(1L, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("EPIDEMIOLOGY")
                .hasMessageContaining("epidemiology");
    }

    @Test
    void applyToDisease_allKnownSectionTypes_shouldMap() {
        // Verify every DraftSectionType maps to its lowercase counterpart without null
        var disease = new Disease(); disease.setId(1L);
        draft.setDisease(disease);
        draft.setStatus(DraftStatus.APPROVED);

        // Build one section per DraftSectionType
        var allSections = java.util.Arrays.stream(DraftSectionType.values())
                .map(t -> DiseaseDraftSection.builder()
                        .id((long) t.ordinal())
                        .draft(draft)
                        .sectionType(t)
                        .title(t.name())
                        .content("Content for " + t.name())
                        .orderIndex(t.ordinal())
                        .build())
                .toList();

        var savedVersion = DiseaseVersion.builder().id(10L).disease(disease).versionNumber(1)
                .status(com.duoq.medlearn.knowledge.version.enums.VersionStatus.APPROVED).build();

        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(allSections);
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(diseaseVersionRepository.findMaxVersionNumberByDiseaseId(1L)).thenReturn(Optional.empty());
        when(diseaseVersionRepository.save(any(DiseaseVersion.class))).thenReturn(savedVersion);
        // Every DraftSectionType.name().toLowerCase() must return a SectionType
        for (DraftSectionType t : DraftSectionType.values()) {
            when(sectionTypeRepository.findByName(t.name().toLowerCase()))
                    .thenReturn(Optional.of(
                            com.duoq.medlearn.knowledge.section.entity.SectionType.builder()
                                    .id(t.ordinal() + 1).name(t.name().toLowerCase()).build()));
        }
        when(diseaseSectionRepository.save(any(DiseaseSection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(diseaseRepository.save(any(Disease.class))).thenReturn(disease);

        // Must NOT throw
        lifecycleService.applyToDisease(1L, userId);

        // All 17 sections saved
        verify(diseaseSectionRepository, times(DraftSectionType.values().length))
                .save(any(DiseaseSection.class));
    }

    @Test
    void applyToDisease_shouldArchiveOldVersionAndSetCurrentVersion() {
        var disease = new Disease(); disease.setId(1L);
        draft.setDisease(disease);
        draft.setStatus(DraftStatus.APPROVED);

        var savedVersion = DiseaseVersion.builder().id(10L).disease(disease).versionNumber(2)
                .status(com.duoq.medlearn.knowledge.version.enums.VersionStatus.APPROVED).build();

        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(section));
        when(userRepository.findByIdWithRoles(userId)).thenReturn(Optional.of(User.builder().id(userId).build()));
        when(diseaseVersionRepository.findMaxVersionNumberByDiseaseId(1L)).thenReturn(Optional.of(1));
        when(diseaseVersionRepository.save(any(DiseaseVersion.class))).thenReturn(savedVersion);
        when(sectionTypeRepository.findByName("definition"))
                .thenReturn(Optional.of(com.duoq.medlearn.knowledge.section.entity.SectionType.builder()
                        .id(1).name("definition").build()));
        when(diseaseSectionRepository.save(any(DiseaseSection.class))).thenAnswer(inv -> inv.getArgument(0));
        when(diseaseRepository.save(any(Disease.class))).thenReturn(disease);

        lifecycleService.applyToDisease(1L, userId);

        // Old approved versions archived
        verify(diseaseVersionRepository).archiveApprovedVersionsExcept(disease.getId(), savedVersion.getId());
        // Disease.currentVersion = new version
        assertThat(disease.getCurrentVersion()).isEqualTo(savedVersion);
        // Draft marked ARCHIVED
        assertThat(draft.getStatus()).isEqualTo(DraftStatus.ARCHIVED);
    }
}
