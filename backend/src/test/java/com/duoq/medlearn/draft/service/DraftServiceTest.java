package com.duoq.medlearn.draft.service;

import com.duoq.medlearn.document.exception.DocumentProcessingException;
import com.duoq.medlearn.draft.dto.CreateDraftRequest;
import com.duoq.medlearn.draft.dto.DiseaseDraftResponse;
import com.duoq.medlearn.draft.dto.DiseaseDraftSectionResponse;
import com.duoq.medlearn.draft.dto.UpdateDraftRequest;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.entity.DiseaseDraftSection;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftSectionType;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.draft.mapper.DraftMapper;
import com.duoq.medlearn.draft.repository.DiseaseDraftRepository;
import com.duoq.medlearn.draft.repository.DiseaseDraftSectionRepository;
import com.duoq.medlearn.draft.service.impl.DraftServiceImpl;
import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.repository.DiseaseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DraftServiceTest {

    @Mock private DiseaseDraftRepository draftRepository;
    @Mock private DiseaseDraftSectionRepository sectionRepository;
    @Mock private DiseaseRepository diseaseRepository;
    @Mock private DraftMapper draftMapper;
    @InjectMocks private DraftServiceImpl draftService;

    private final Long userId = 1L;
    private DiseaseDraft testDraft;
    private DiseaseDraftResponse testResponse;
    private DiseaseDraftSection testSection;
    private DiseaseDraftSectionResponse testSectionResponse;
    private Disease testDisease;

    @BeforeEach
    void setUp() {
        testDisease = new Disease();
        testDisease.setId(1L);
        testDisease.setName("Diabetes");

        testDraft = DiseaseDraft.builder()
                .id(1L).title("Diabetes Draft").status(DraftStatus.DRAFT)
                .sourceMethod(DraftMethod.MANUAL).disease(testDisease)
                .createdBy(User.builder().id(userId).build()).build();

        testSection = DiseaseDraftSection.builder()
                .id(1L).draft(testDraft).sectionType(DraftSectionType.DEFINITION)
                .title("Định nghĩa").content("Nội dung").orderIndex(0)
                .wordCount(2).aiGenerated(false).build();

        testSectionResponse = DiseaseDraftSectionResponse.builder()
                .id(1L).sectionType(DraftSectionType.DEFINITION)
                .title("Định nghĩa").content("Nội dung").orderIndex(0).aiGenerated(false).build();

        testResponse = DiseaseDraftResponse.builder()
                .id(1L).diseaseId(1L).diseaseName("Diabetes").title("Diabetes Draft")
                .status(DraftStatus.DRAFT).sourceMethod(DraftMethod.MANUAL).createdBy(userId)
                .sections(List.of(testSectionResponse)).build();
    }

    @Test
    void create_shouldSucceed_withDisease() {
        var sections = List.of(new CreateDraftRequest.SectionContent(
                DraftSectionType.DEFINITION, "Định nghĩa", "Nội dung", 0));
        var req = new CreateDraftRequest(1L, "Diabetes Draft", DraftMethod.MANUAL, null, sections);

        when(diseaseRepository.findById(1L)).thenReturn(Optional.of(testDisease));
        when(draftRepository.save(any(DiseaseDraft.class))).thenReturn(testDraft);
        when(sectionRepository.save(any(DiseaseDraftSection.class))).thenReturn(testSection);
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(testSection));
        when(draftMapper.toResponse(testDraft)).thenReturn(testResponse.toBuilder().sections(null).build());
        when(draftMapper.toSectionResponseList(anyList())).thenReturn(List.of(testSectionResponse));

        var result = draftService.create(req, userId);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Diabetes Draft");
        verify(draftRepository).save(any(DiseaseDraft.class));
        verify(sectionRepository, atLeastOnce()).save(any(DiseaseDraftSection.class));
    }

    @Test
    void create_shouldSucceed_withoutDisease() {
        var req = new CreateDraftRequest(null, "Standalone Draft", null, null, List.of());

        var draftNoDisease = DiseaseDraft.builder()
                .id(2L).title("Standalone Draft").status(DraftStatus.DRAFT)
                .sourceMethod(DraftMethod.MANUAL).disease(null)
                .createdBy(User.builder().id(userId).build()).build();

        var respNoDisease = DiseaseDraftResponse.builder()
                .id(2L).title("Standalone Draft").status(DraftStatus.DRAFT)
                .sourceMethod(DraftMethod.MANUAL).createdBy(userId).sections(List.of()).build();

        when(draftRepository.save(any(DiseaseDraft.class))).thenReturn(draftNoDisease);
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(2L))
                .thenReturn(List.of());
        when(draftMapper.toResponse(draftNoDisease)).thenReturn(respNoDisease.toBuilder().sections(null).build());
        when(draftMapper.toSectionResponseList(List.of())).thenReturn(List.of());

        var result = draftService.create(req, userId);

        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("Standalone Draft");
        verify(diseaseRepository, never()).findById(anyLong());
    }

    @Test
    void create_shouldThrow_whenDiseaseNotFound() {
        var req = new CreateDraftRequest(999L, "Bad Draft", null, null, List.of());

        when(diseaseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> draftService.create(req, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getById_shouldReturnDraft() {
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDraft));
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(testSection));
        when(draftMapper.toResponse(testDraft)).thenReturn(testResponse.toBuilder().sections(null).build());
        when(draftMapper.toSectionResponseList(anyList())).thenReturn(List.of(testSectionResponse));

        var result = draftService.getById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getById_shouldThrow_whenNotFound() {
        when(draftRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> draftService.getById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void listByUser_shouldReturnPage() {
        var page = new PageImpl<>(List.of(testDraft));
        when(draftRepository.findAllByCreatedByIdAndDeletedAtIsNull(eq(userId), any(Pageable.class)))
                .thenReturn(page);
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(testSection));
        when(draftMapper.toResponse(testDraft)).thenReturn(testResponse.toBuilder().sections(null).build());
        when(draftMapper.toSectionResponseList(anyList())).thenReturn(List.of(testSectionResponse));

        var result = draftService.listByUser(userId, Pageable.unpaged());

        assertThat(result).hasSize(1);
    }

    @Test
    void listByDisease_shouldReturnPage() {
        var page = new PageImpl<>(List.of(testDraft));
        when(draftRepository.findAllByDiseaseIdAndDeletedAtIsNull(eq(1L), any(Pageable.class)))
                .thenReturn(page);
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(testSection));
        when(draftMapper.toResponse(testDraft)).thenReturn(testResponse.toBuilder().sections(null).build());
        when(draftMapper.toSectionResponseList(anyList())).thenReturn(List.of(testSectionResponse));

        var result = draftService.listByDisease(1L, Pageable.unpaged());

        assertThat(result).hasSize(1);
    }

    @Test
    void update_shouldSucceed() {
        var sections = List.of(new UpdateDraftRequest.SectionContent(
                null, DraftSectionType.DEFINITION, "New Title", "New Content", 0));
        var req = new UpdateDraftRequest("Updated Title", sections);

        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDraft));
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(testSection));
        when(sectionRepository.save(any(DiseaseDraftSection.class))).thenReturn(testSection);
        when(sectionRepository.saveAll(anyList())).thenReturn(List.of(testSection));
        when(draftMapper.toResponse(testDraft)).thenReturn(testResponse.toBuilder().sections(null).build());
        when(draftMapper.toSectionResponseList(anyList())).thenReturn(List.of(testSectionResponse));
        // second call for buildResponse
        when(sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(1L))
                .thenReturn(List.of(testSection));

        var result = draftService.update(1L, req, userId);

        assertThat(result).isNotNull();
        verify(sectionRepository).saveAll(anyList());
    }

    @Test
    void update_shouldThrow_whenNotOwner() {
        var req = new UpdateDraftRequest("x", List.of());
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDraft));

        assertThatThrownBy(() -> draftService.update(1L, req, 999L))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Cannot update another user's draft");
    }

    @Test
    void update_shouldThrow_whenNotDraftStatus() {
        testDraft.setStatus(DraftStatus.PENDING_REVIEW);
        var req = new UpdateDraftRequest("x", List.of());
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDraft));

        assertThatThrownBy(() -> draftService.update(1L, req, userId))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Only DRAFT drafts can be updated");
    }

    @Test
    void delete_shouldSoftDeleteOwnDraft() {
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDraft));

        draftService.delete(1L, userId);

        assertThat(testDraft.getDeletedAt()).isNotNull();
        verify(draftRepository).save(testDraft);
    }

    @Test
    void delete_shouldThrow_whenNotOwner() {
        when(draftRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(testDraft));

        assertThatThrownBy(() -> draftService.delete(1L, 999L))
                .isInstanceOf(DocumentProcessingException.class)
                .hasMessageContaining("Cannot delete another user's draft");
    }

    @Test
    void delete_shouldThrow_whenNotFound() {
        when(draftRepository.findByIdAndDeletedAtIsNull(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> draftService.delete(999L, userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
