package com.duoq.medlearn.draft.service.impl;

import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.draft.dto.DiseaseDraftResponse;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.draft.mapper.DraftMapper;
import com.duoq.medlearn.draft.repository.DiseaseDraftRepository;
import com.duoq.medlearn.draft.repository.DiseaseDraftSectionRepository;
import com.duoq.medlearn.draft.service.DraftLifecycleService;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftLifecycleServiceImpl implements DraftLifecycleService {

    private final DiseaseDraftRepository draftRepository;
    private final DiseaseDraftSectionRepository sectionRepository;
    private final DraftMapper draftMapper;

    @Override
    @Transactional
    public DiseaseDraftResponse submitForReview(Long draftId, Long userId) {
        var draft = findDraft(draftId);
        if (draft.getStatus() != DraftStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT drafts can be submitted for review");
        }
        draft.setStatus(DraftStatus.PENDING_REVIEW);
        draftRepository.save(draft);
        log.info("Draft submitted for review: id={}", draftId);
        return buildResponse(draft);
    }

    @Override
    @Transactional
    public DiseaseDraftResponse approve(Long draftId, Long reviewerId, String note) {
        var draft = findDraft(draftId);
        if (draft.getStatus() != DraftStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Only PENDING_REVIEW drafts can be approved");
        }
        draft.setStatus(DraftStatus.APPROVED);
        draft.setReviewedBy(User.builder().id(reviewerId).build());
        draft.setReviewedAt(OffsetDateTime.now());
        draft.setReviewNote(note);
        draftRepository.save(draft);
        log.info("Draft approved: id={}, reviewerId={}", draftId, reviewerId);
        return buildResponse(draft);
    }

    @Override
    @Transactional
    public DiseaseDraftResponse reject(Long draftId, Long reviewerId, String reason) {
        var draft = findDraft(draftId);
        if (draft.getStatus() != DraftStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Only PENDING_REVIEW drafts can be rejected");
        }
        draft.setStatus(DraftStatus.REJECTED);
        draft.setReviewedBy(User.builder().id(reviewerId).build());
        draft.setReviewedAt(OffsetDateTime.now());
        draft.setReviewNote(reason);
        draftRepository.save(draft);
        log.info("Draft rejected: id={}, reviewerId={}, reason={}", draftId, reviewerId, reason);
        return buildResponse(draft);
    }

    @Override
    @Transactional
    public DiseaseDraftResponse archive(Long draftId, Long userId) {
        var draft = findDraft(draftId);
        draft.setStatus(DraftStatus.ARCHIVED);
        draftRepository.save(draft);
        log.info("Draft archived: id={}", draftId);
        return buildResponse(draft);
    }

    @Override
    @Transactional
    public void applyToDisease(Long draftId, Long userId) {
        var draft = findDraft(draftId);
        if (draft.getStatus() != DraftStatus.APPROVED) {
            throw new IllegalStateException("Only APPROVED drafts can be applied to a disease");
        }
        log.warn("applyToDisease not yet implemented: draftId={}, userId={}", draftId, userId);
    }

    @Override
    @Transactional
    public DiseaseDraftResponse cloneDraft(Long draftId, Long userId) {
        var source = findDraft(draftId);
        var sections = sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(source.getId());

        var clone = DiseaseDraft.builder()
                .disease(source.getDisease())
                .title(source.getTitle() + " (Copy)")
                .sourceMethod(DraftMethod.MANUAL)
                .createdBy(User.builder().id(userId).build())
                .build();
        var saved = draftRepository.save(clone);

        for (var s : sections) {
            var clonedSection = s.toBuilder()
                    .id(null)
                    .draft(saved)
                    .build();
            sectionRepository.save(clonedSection);
        }

        log.info("Draft cloned: sourceId={}, cloneId={}", draftId, saved.getId());
        return buildResponse(saved);
    }

    private DiseaseDraft findDraft(Long id) {
        return draftRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Draft not found: " + id));
    }

    private DiseaseDraftResponse buildResponse(DiseaseDraft draft) {
        var sections = sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(draft.getId());
        return draftMapper.toResponse(draft).toBuilder()
                .sections(draftMapper.toSectionResponseList(sections))
                .build();
    }
}
