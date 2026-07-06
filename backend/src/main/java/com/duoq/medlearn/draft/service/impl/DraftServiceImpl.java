package com.duoq.medlearn.draft.service.impl;

import com.duoq.medlearn.document.entity.Document;
import com.duoq.medlearn.draft.dto.CreateDraftRequest;
import com.duoq.medlearn.draft.dto.DiseaseDraftResponse;
import com.duoq.medlearn.draft.dto.UpdateDraftRequest;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.entity.DiseaseDraftSection;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.draft.mapper.DraftMapper;
import com.duoq.medlearn.draft.repository.DiseaseDraftRepository;
import com.duoq.medlearn.draft.repository.DiseaseDraftSectionRepository;
import com.duoq.medlearn.draft.service.DraftService;
import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.repository.DiseaseRepository;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftServiceImpl implements DraftService {

    private final DiseaseDraftRepository draftRepository;
    private final DiseaseDraftSectionRepository sectionRepository;
    private final DiseaseRepository diseaseRepository;
    private final DraftMapper draftMapper;

    @Override
    @Transactional
    public DiseaseDraftResponse create(CreateDraftRequest request, Long userId) {
        Disease disease = null;
        if (request.getDiseaseId() != null) {
            disease = diseaseRepository.findById(request.getDiseaseId())
                    .orElseThrow(() -> new ResourceNotFoundException("Disease not found: " + request.getDiseaseId()));
        }

        var draft = DiseaseDraft.builder()
                .disease(disease)
                .title(request.getTitle())
                .sourceMethod(request.getSourceMethod() != null ? request.getSourceMethod() : DraftMethod.MANUAL)
                .sourceDocument(request.getSourceDocumentId() != null
                        ? Document.builder().id(request.getSourceDocumentId()).build() : null)
                .createdBy(User.builder().id(userId).build())
                .build();

        var saved = draftRepository.save(draft);

        int order = 0;
        for (var sc : request.getSections()) {
            var section = DiseaseDraftSection.builder()
                    .draft(saved)
                    .sectionType(sc.getSectionType())
                    .title(sc.getTitle())
                    .content(sc.getContent())
                    .orderIndex(sc.getOrderIndex() != null ? sc.getOrderIndex() : order++)
                    .wordCount(countWords(sc.getContent()))
                    .aiGenerated(false)
                    .build();
            sectionRepository.save(section);
        }

        log.info("Draft created: id={}, title={}, sections={}", saved.getId(), saved.getTitle(), request.getSections().size());
        return buildResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseDraftResponse getById(Long id) {
        var draft = draftRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found: " + id));
        return buildResponse(draft);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseDraftResponse> listByUser(Long userId, Pageable pageable) {
        return draftRepository.findAllByCreatedByIdAndDeletedAtIsNull(userId, pageable)
                .map(this::buildResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseDraftResponse> listByDisease(Long diseaseId, Pageable pageable) {
        return draftRepository.findAllByDiseaseIdAndDeletedAtIsNull(diseaseId, pageable)
                .map(this::buildResponse);
    }

    @Override
    @Transactional
    public DiseaseDraftResponse update(Long id, UpdateDraftRequest request, Long userId) {
        var draft = draftRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found: " + id));

        if (!draft.getCreatedBy().getId().equals(userId)) {
            throw new com.duoq.medlearn.document.exception.DocumentProcessingException("Cannot update another user's draft");
        }

        if (draft.getStatus() != DraftStatus.DRAFT) {
            throw new com.duoq.medlearn.document.exception.DocumentProcessingException("Only DRAFT drafts can be updated");
        }

        if (request.getTitle() != null) {
            draft.setTitle(request.getTitle());
        }

        var existingSections = sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(draft.getId());
        existingSections.forEach(s -> s.setDeletedAt(OffsetDateTime.now()));
        sectionRepository.saveAll(existingSections);

        int order = 0;
        for (var sc : request.getSections()) {
            var section = DiseaseDraftSection.builder()
                    .draft(draft)
                    .sectionType(sc.getSectionType())
                    .title(sc.getTitle())
                    .content(sc.getContent())
                    .orderIndex(sc.getOrderIndex() != null ? sc.getOrderIndex() : order++)
                    .wordCount(countWords(sc.getContent()))
                    .aiGenerated(false)
                    .build();
            sectionRepository.save(section);
        }

        draftRepository.save(draft);
        log.info("Draft updated: id={}", id);
        return buildResponse(draft);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        var draft = draftRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found: " + id));
        if (!draft.getCreatedBy().getId().equals(userId)) {
            throw new com.duoq.medlearn.document.exception.DocumentProcessingException("Cannot delete another user's draft");
        }
        draft.setDeletedAt(OffsetDateTime.now());
        draftRepository.save(draft);
        log.info("Draft soft deleted: id={}", id);
    }

    private DiseaseDraftResponse buildResponse(DiseaseDraft draft) {
        var sections = sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(draft.getId());
        return draftMapper.toResponse(draft).toBuilder()
                .sections(draftMapper.toSectionResponseList(sections))
                .build();
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.split("\\s+").length;
    }
}
