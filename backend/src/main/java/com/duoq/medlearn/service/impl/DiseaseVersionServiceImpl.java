package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.DiseaseSection;
import com.duoq.medlearn.domain.entity.DiseaseVersion;
import com.duoq.medlearn.domain.entity.DiseaseVersionSymptom;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.AuditAction;
import com.duoq.medlearn.domain.enums.VersionStatus;
import com.duoq.medlearn.domain.dto.version.CreateDiseaseVersionRequest;
import com.duoq.medlearn.domain.dto.version.ModerationRequest;
import com.duoq.medlearn.domain.dto.version.UpdateDiseaseVersionRequest;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionDTO;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.repository.*;
import com.duoq.medlearn.mapper.DiseaseMapper;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.domain.enums.PermissionCode;
import com.duoq.medlearn.service.AuditService;
import com.duoq.medlearn.service.PermissionService;
import com.duoq.medlearn.service.DiseaseSectionService;
import com.duoq.medlearn.service.DiseaseVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiseaseVersionServiceImpl implements DiseaseVersionService {

    private final DiseaseVersionRepository diseaseVersionRepository;
    private final DiseaseRepository diseaseRepository;
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;
    private final DiseaseMapper diseaseMapper;
    private final DiseaseSectionRepository diseaseSectionRepository;
    private final DiseaseVersionSymptomRepository diseaseVersionSymptomRepository;
    private final AuditService auditService;
    private final DiseaseSectionService diseaseSectionService;
    private final PermissionService permissionService;

    @Override
    @Transactional
    public DiseaseVersionDTO createDraftVersion(Long diseaseId, CreateDiseaseVersionRequest request) {
        Disease disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));
        User currentUser = findCurrentUser();

        DiseaseVersion version = DiseaseVersion.builder()
                .disease(disease)
                .createdBy(currentUser)
                .versionNumber(generateNextVersionNumber(diseaseId))
                .status(VersionStatus.DRAFT)
                .moderationNote(request != null ? request.getNote() : null)
                .build();

        return diseaseMapper.toDiseaseVersionDTO(diseaseVersionRepository.save(version));
    }

    @Override
    @Transactional
    public DiseaseVersionDTO cloneApprovedVersion(Long diseaseId) {
        DiseaseVersion approved = diseaseVersionRepository.findCurrentVersionByDiseaseId(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Current approved version not found"));

        if (approved.getStatus() != VersionStatus.APPROVED) {
            throw new IllegalStateException("Current version is not approved");
        }

        Disease disease = approved.getDisease();
        User currentUser = findCurrentUser();

        DiseaseVersion clone = DiseaseVersion.builder()
                .disease(disease)
                .createdBy(currentUser)
                .versionNumber(generateNextVersionNumber(diseaseId))
                .status(VersionStatus.DRAFT)
                .moderationNote(approved.getModerationNote())
                .build();

        clone = diseaseVersionRepository.save(clone);

        cloneSections(approved, clone);
        cloneSymptoms(approved, clone);

        return diseaseMapper.toDiseaseVersionDTO(clone);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseVersionDTO getVersionById(Long versionId) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));
        return diseaseMapper.toDiseaseVersionDTO(version);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiseaseVersionDTO> getDiseaseVersions(Long diseaseId) {
        return diseaseVersionRepository.findAllByDiseaseIdAndDeletedAtIsNullOrderByVersionNumberDesc(diseaseId)
                .stream()
                .map(diseaseMapper::toDiseaseVersionDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseVersionDTO getCurrentApprovedVersion(Long diseaseId) {
        DiseaseVersion version = diseaseVersionRepository.findCurrentVersionByDiseaseId(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Current approved version not found"));
        return diseaseMapper.toDiseaseVersionDTO(version);
    }

    @Override
    @Transactional(readOnly = true)
    public DiseaseVersionDTO getLatestDraftVersion(Long diseaseId) {
        return diseaseVersionRepository.findAllByDiseaseIdAndDeletedAtIsNullOrderByVersionNumberDesc(diseaseId)
                .stream()
                .filter(v -> v.getStatus() == VersionStatus.DRAFT)
                .findFirst()
                .map(diseaseMapper::toDiseaseVersionDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Draft version not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DiseaseVersionDTO> getPendingReviewVersions(Pageable pageable) {
        return diseaseVersionRepository.findAllByStatusAndDeletedAtIsNull(VersionStatus.PENDING_REVIEW, pageable)
                .map(diseaseMapper::toDiseaseVersionDTO);
    }

    @Override
    @Transactional
    public DiseaseVersionDTO updateDraftVersion(Long versionId, UpdateDiseaseVersionRequest request) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));

        if (version.getStatus() != VersionStatus.DRAFT) {
            throw new IllegalStateException("Only draft version can be updated");
        }

        validateVersionOwnership(versionId);
        version.setModerationNote(request.getModerationNote());

        return diseaseMapper.toDiseaseVersionDTO(diseaseVersionRepository.save(version));
    }

    @Override
    @Transactional
    public DiseaseVersionDTO submitForReview(Long versionId) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));

        // Idempotency check: Prevent duplicate submits
        if (version.getStatus() == VersionStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Version is already pending review.");
        }

        validateWorkflowTransition(version.getStatus(), VersionStatus.PENDING_REVIEW);
        validateVersionOwnership(versionId);
        diseaseSectionService.validateRequiredSections(versionId);

        version.submitForReview();
        DiseaseVersionDTO result = diseaseMapper.toDiseaseVersionDTO(diseaseVersionRepository.save(version));
        auditService.log(findCurrentUser(), AuditAction.VERSION_SUBMITTED,
                "DiseaseVersion", versionId,
                Map.of("diseaseId", version.getDisease().getId(),
                       "versionNumber", version.getVersionNumber()));
        return result;
    }

    @Override
    @Transactional
    public DiseaseVersionDTO approveVersion(Long versionId, ModerationRequest request) {
        validateReviewerPermission();

        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));

        validateWorkflowTransition(version.getStatus(), VersionStatus.APPROVED);

        User reviewer = findCurrentUser();
        if (version.getCreatedBy() != null && version.getCreatedBy().getId().equals(reviewer.getId())) {
            throw new IllegalStateException("Reviewer cannot approve own version");
        }

        Disease disease = diseaseRepository.findByIdForUpdate(version.getDisease().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));

        diseaseVersionRepository.archiveApprovedVersionsExcept(disease.getId(), versionId);

        version.approve(reviewer);
        if (request != null && request.getNote() != null) {
            version.setModerationNote(request.getNote());
        }

        disease.setCurrentVersion(version);
        diseaseRepository.save(disease);

        DiseaseVersionDTO result = diseaseMapper.toDiseaseVersionDTO(diseaseVersionRepository.save(version));
        auditService.log(reviewer, AuditAction.VERSION_APPROVED,
                "DiseaseVersion", versionId,
                Map.of("diseaseId", disease.getId(),
                       "versionNumber", version.getVersionNumber(),
                       "reviewerId", reviewer.getId()),
                request != null ? request.getNote() : null);
        return result;
    }

    @Override
    @Transactional
    public DiseaseVersionDTO rejectVersion(Long versionId, ModerationRequest request) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));

        validateReviewerPermission();
        validateWorkflowTransition(version.getStatus(), VersionStatus.REJECTED);

        User reviewer = findCurrentUser();
        String note = request != null ? request.getNote() : null;
        version.reject(reviewer, note);

        DiseaseVersionDTO result = diseaseMapper.toDiseaseVersionDTO(diseaseVersionRepository.save(version));
        auditService.log(reviewer, AuditAction.VERSION_REJECTED,
                "DiseaseVersion", versionId,
                Map.of("diseaseId", version.getDisease().getId(),
                       "versionNumber", version.getVersionNumber(),
                       "reviewerId", reviewer.getId()),
                note);
        return result;
    }

    @Override
    @Transactional
    public DiseaseVersionDTO archiveVersion(Long versionId) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));

        validateReviewerPermission();
        validateWorkflowTransition(version.getStatus(), VersionStatus.ARCHIVED);
        version.setStatus(VersionStatus.ARCHIVED);
        return diseaseMapper.toDiseaseVersionDTO(diseaseVersionRepository.save(version));
    }

    @Override
    @Transactional
    public void softDeleteVersion(Long versionId) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));

        // Prevent deletion of approved versions to protect public API contract
        if (version.getStatus() == VersionStatus.APPROVED) {
            throw new IllegalStateException("Cannot delete approved version. Archive instead.");
        }

        version.setDeletedAt(OffsetDateTime.now());
        diseaseVersionRepository.save(version);
    }

    @Override
    @Transactional
    public void restoreVersion(Long versionId) {
        // Use findByIdIgnoreDeletedAt to bypass @SQLRestriction for soft-deleted entities
        DiseaseVersion version = diseaseVersionRepository.findByIdIgnoreDeletedAt(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));

        // Prevent re-restoring a non-deleted version
        if (version.getDeletedAt() == null) {
            throw new IllegalStateException("Version is not currently deleted.");
        }

        version.setDeletedAt(null);
        diseaseVersionRepository.save(version);
    }

    // ===================================
    // HELPER METHODS
    // ===================================

    private void validateWorkflowTransition(VersionStatus currentStatus, VersionStatus targetStatus) {
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("Invalid workflow transition: " + currentStatus + " -> " + targetStatus);
        }
    }

    private boolean canEditVersion(Long versionId) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));
        return version.getStatus() == VersionStatus.DRAFT;
    }

    private boolean isApprovedVersion(Long versionId) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));
        return version.getStatus() == VersionStatus.APPROVED;
    }

    private boolean isPendingReview(Long versionId) {
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));
        return version.getStatus() == VersionStatus.PENDING_REVIEW;
    }

    private void validateVersionOwnership(Long versionId) {
        Long currentUserId = currentUserResolver.resolveCurrentUserId();
        DiseaseVersion version = diseaseVersionRepository.findById(versionId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease version not found"));

        // Check if user has SECTION_EDIT_ANY permission (can bypass ownership)
        if (permissionService.hasPermission(PermissionCode.SECTION_EDIT_ANY)) {
            return;
        }

        if (!version.getCreatedBy().getId().equals(currentUserId)) {
            throw new IllegalStateException("Current user is not owner of this version");
        }
    }

    private void validateReviewerPermission() {
        if (!permissionService.hasPermission(PermissionCode.VERSION_REVIEW)) {
            throw new IllegalStateException("VERSION_REVIEW permission required");
        }
    }

    private Integer generateNextVersionNumber(Long diseaseId) {
        return diseaseVersionRepository.findMaxVersionNumberByDiseaseId(diseaseId)
                .map(max -> max + 1)
                .orElse(1);
    }

    @Transactional
    private void deactivatePreviousApprovedVersion(Long diseaseId) {
        diseaseVersionRepository.findCurrentVersionByDiseaseId(diseaseId)
                .ifPresent(version -> {
                    if (version.getStatus() == VersionStatus.APPROVED) {
                        version.setStatus(VersionStatus.ARCHIVED);
                        diseaseVersionRepository.save(version);
                    }
                });
    }

    private User findCurrentUser() {
        Long userId = currentUserResolver.resolveCurrentUserId();
        return userRepository.findByIdWithRoles(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void cloneSections(DiseaseVersion source, DiseaseVersion target) {
        List<DiseaseSection> sourceSections = diseaseSectionRepository.findAllByDiseaseVersionIdAndDeletedAtIsNullOrderByOrderIndexAsc(source.getId());
        for (DiseaseSection sourceSection : sourceSections) {
            DiseaseSection clonedSection = DiseaseSection.builder()
                    .diseaseVersion(target)
                    .sectionType(sourceSection.getSectionType())
                    .title(sourceSection.getTitle())
                    .content(sourceSection.getContent())
                    .orderIndex(sourceSection.getOrderIndex())
                    .build();
            diseaseSectionRepository.save(clonedSection);
        }
    }

    private void cloneSymptoms(DiseaseVersion source, DiseaseVersion target) {
        List<DiseaseVersionSymptom> sourceSymptoms = diseaseVersionSymptomRepository.findAllByDiseaseVersionId(source.getId());
        for (DiseaseVersionSymptom sourceSymptom : sourceSymptoms) {
            DiseaseVersionSymptom clonedSymptom = DiseaseVersionSymptom.builder()
                    .diseaseVersion(target)
                    .symptom(sourceSymptom.getSymptom())
                    .weightScore(sourceSymptom.getWeightScore())
                    .build();
            diseaseVersionSymptomRepository.save(clonedSymptom);
        }
    }

}
