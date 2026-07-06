package com.duoq.medlearn.draft.service;

import com.duoq.medlearn.draft.dto.DiseaseDraftResponse;
import com.duoq.medlearn.draft.dto.DiseaseDraftSectionResponse;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.entity.DiseaseDraftSection;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftSectionType;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.draft.mapper.DraftMapper;
import com.duoq.medlearn.draft.repository.DiseaseDraftRepository;
import com.duoq.medlearn.draft.repository.DiseaseDraftSectionRepository;
import com.duoq.medlearn.draft.service.impl.DraftLifecycleServiceImpl;
import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.exception.ResourceNotFoundException;
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
    void submitForReview_shouldThrow_whenNotDraft() {
        draft.setStatus(DraftStatus.PENDING_REVIEW);
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));

        assertThatThrownBy(() -> lifecycleService.submitForReview(1L, userId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only DRAFT drafts");
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
    void applyToDisease_shouldSucceed_whenApproved() {
        draft.setStatus(DraftStatus.APPROVED);
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(draft));

        lifecycleService.applyToDisease(1L, userId);

        // Currently a no-op, just verifies no exception
        verify(draftRepository).findByIdAndDeletedAtIsNull(1L);
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
}
