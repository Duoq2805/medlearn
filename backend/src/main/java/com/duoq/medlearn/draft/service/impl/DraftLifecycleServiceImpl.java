package com.duoq.medlearn.draft.service.impl;

import com.duoq.medlearn.audit.enums.AuditAction;
import com.duoq.medlearn.audit.service.AuditService;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.auth.repository.UserRepository;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.draft.dto.response.DiseaseDraftResponse;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.entity.DiseaseDraftSection;
import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftStatus;
import com.duoq.medlearn.draft.mapper.DraftMapper;
import com.duoq.medlearn.draft.repository.DiseaseDraftRepository;
import com.duoq.medlearn.draft.repository.DiseaseDraftSectionRepository;
import com.duoq.medlearn.draft.service.DraftLifecycleService;
import com.duoq.medlearn.knowledge.disease.entity.Disease;
import com.duoq.medlearn.knowledge.disease.entity.DiseaseSection;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.section.entity.SectionType;
import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;
import com.duoq.medlearn.knowledge.section.repository.SectionTypeRepository;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DraftLifecycleServiceImpl implements DraftLifecycleService {

    private final DiseaseDraftRepository draftRepository;
    private final DiseaseDraftSectionRepository sectionRepository;
    private final DraftMapper draftMapper;
    private final DiseaseRepository diseaseRepository;
    private final DiseaseVersionRepository diseaseVersionRepository;
    private final DiseaseSectionRepository diseaseSectionRepository;
    private final SectionTypeRepository sectionTypeRepository;
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;
    private final AuditService auditService;

    @Override
    @Transactional
    public DiseaseDraftResponse submitForReview(Long draftId, Long userId) {
        var draft = findDraft(draftId);
        if (draft.getStatus() != DraftStatus.DRAFT && draft.getStatus() != DraftStatus.REJECTED) {
            throw new IllegalStateException("Only DRAFT or REJECTED drafts can be submitted for review");
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

        Disease disease = draft.getDisease();
        if (disease == null) {
            // Draft WITHOUT a disease: cannot map to a version — require explicit disease binding.
            throw new IllegalStateException(
                    "Draft has no associated disease. Create the draft from an existing disease "
                            + "or bind one before applying.");
        }

        var draftSections = sectionRepository.findAllByDraftIdAndDeletedAtIsNullOrderByOrderIndex(draft.getId());
        if (draftSections.isEmpty()) {
            throw new IllegalStateException("Draft has no sections to apply");
        }

        User currentUser = userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        // 1) Create a new APPROVED version from the draft content
        int nextVersionNumber = diseaseVersionRepository
                .findMaxVersionNumberByDiseaseId(disease.getId())
                .map(n -> n + 1)
                .orElse(1);

        DiseaseVersion version = DiseaseVersion.builder()
                .disease(disease)
                .createdBy(currentUser)
                .versionNumber(nextVersionNumber)
                .status(VersionStatus.APPROVED)
                .reviewedBy(currentUser)
                .reviewedAt(OffsetDateTime.now())
                .moderationNote(draft.getReviewNote())
                .build();
        version = diseaseVersionRepository.save(version);

        // 2) Clone draft sections into disease sections (map DraftSectionType -> SectionType by lowercase name)
        int order = 0;
        for (DiseaseDraftSection draftSection : draftSections) {
            String sectionTypeName = draftSection.getSectionType().name().toLowerCase();
            SectionType sectionType = sectionTypeRepository.findByName(sectionTypeName)
                    .orElseThrow(() -> new IllegalStateException(
                            "Cannot apply draft: no SectionType found for DraftSectionType '"
                                    + draftSection.getSectionType().name() + "' (looked up as '" + sectionTypeName + "'). "
                                    + "Ensure all DraftSectionType values are seeded in the section_type table."));
            DiseaseSection section = DiseaseSection.builder()
                    .diseaseVersion(version)
                    .sectionType(sectionType)
                    .title(draftSection.getTitle())
                    .content(draftSection.getContent())
                    .orderIndex(draftSection.getOrderIndex() != null ? draftSection.getOrderIndex() : order)
                    .build();
            diseaseSectionRepository.save(section);
            order++;
        }

        // 3) Archive old approved versions
        diseaseVersionRepository.archiveApprovedVersionsExcept(disease.getId(), version.getId());

        // 4) Point disease.currentVersion to the new version
        disease.setCurrentVersion(version);
        diseaseRepository.save(disease);

        // 5) Mark draft as ARCHIVED (consumed)
        draft.setStatus(DraftStatus.ARCHIVED);
        draftRepository.save(draft);

        auditService.log(currentUser, AuditAction.VERSION_APPROVED,
                "DiseaseVersion", version.getId(),
                java.util.Map.of("diseaseId", disease.getId(), "versionNumber", version.getVersionNumber(),
                        "sourceDraftId", draft.getId()),
                "Applied from approved draft " + draftId);

        log.info("Draft applied to disease: draftId={}, diseaseId={}, versionNumber={}",
                draftId, disease.getId(), nextVersionNumber);
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
