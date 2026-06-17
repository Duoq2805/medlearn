package com.duoq.medlearn.service.impl;

import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.DiseaseSection;
import com.duoq.medlearn.domain.entity.DiseaseVersion;
import com.duoq.medlearn.domain.entity.DiseaseVersionSymptom;
import com.duoq.medlearn.domain.entity.User;
import com.duoq.medlearn.domain.enums.AuditAction;
import com.duoq.medlearn.domain.enums.VersionStatus;
import com.duoq.medlearn.domain.dto.version.ModerationRequest;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionDTO;
import com.duoq.medlearn.exception.ResourceNotFoundException;
import com.duoq.medlearn.mapper.DiseaseMapper;
import com.duoq.medlearn.repository.*;
import com.duoq.medlearn.security.CurrentUserResolver;
import com.duoq.medlearn.domain.enums.PermissionCode;
import com.duoq.medlearn.service.AuditService;
import com.duoq.medlearn.service.PermissionService;
import com.duoq.medlearn.service.DiseaseWorkflowService;
import com.duoq.medlearn.service.DiseaseVersionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DiseaseWorkflowServiceImpl implements DiseaseWorkflowService {

    private final DiseaseVersionService diseaseVersionService;
    private final DiseaseVersionRepository diseaseVersionRepository;
    private final DiseaseRepository diseaseRepository;
    private final DiseaseMapper diseaseMapper;
    private final DiseaseSectionRepository diseaseSectionRepository;
    private final DiseaseVersionSymptomRepository diseaseVersionSymptomRepository;
    private final UserRepository userRepository;
    private final CurrentUserResolver currentUserResolver;
    private final AuditService auditService;
    private final PermissionService permissionService;

    @Override
    @Transactional
    public DiseaseVersionDTO submit(Long versionId) {
        return diseaseVersionService.submitForReview(versionId);
    }

    @Override
    @Transactional
    public DiseaseVersionDTO approve(Long versionId, ModerationRequest request) {
        return diseaseVersionService.approveVersion(versionId, request);
    }

    @Override
    @Transactional
    public DiseaseVersionDTO reject(Long versionId, ModerationRequest request) {
        return diseaseVersionService.rejectVersion(versionId, request);
    }

    @Override
    @Transactional
    public DiseaseVersionDTO rollback(Long diseaseId, Long targetVersionId) {
        validateReviewerPermission();

        Disease disease = diseaseRepository.findByIdForUpdate(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found"));

        DiseaseVersion targetVersion = diseaseVersionRepository.findById(targetVersionId)
                .orElseThrow(() -> new ResourceNotFoundException("Target version not found"));

        if (targetVersion.getDisease() == null || !targetVersion.getDisease().getId().equals(diseaseId)) {
            throw new IllegalStateException("Target version does not belong to disease");
        }
        if (targetVersion.getStatus() != VersionStatus.APPROVED) {
            throw new IllegalStateException("Only approved version can be rolled back");
        }

        User reviewer = findCurrentUser();
        DiseaseVersion snapshot = DiseaseVersion.builder()
                .disease(disease)
                .createdBy(targetVersion.getCreatedBy() != null ? targetVersion.getCreatedBy() : reviewer)
                .versionNumber(generateNextVersionNumber(diseaseId))
                .status(VersionStatus.APPROVED)
                .moderationNote(targetVersion.getModerationNote())
                .reviewedBy(reviewer)
                .reviewedAt(java.time.OffsetDateTime.now())
                .build();

        snapshot = diseaseVersionRepository.save(snapshot);

        cloneSections(targetVersion, snapshot);
        cloneSymptoms(targetVersion, snapshot);

        diseaseVersionRepository.archiveApprovedVersionsExcept(diseaseId, snapshot.getId());

        Long previousVersionId = disease.getCurrentVersion() != null ? disease.getCurrentVersion().getId() : null;
        disease.setCurrentVersion(snapshot);
        diseaseRepository.save(disease);

        auditService.log(reviewer, AuditAction.VERSION_ROLLBACK,
                "Disease", diseaseId,
                previousVersionId != null ? Map.of("currentVersionId", previousVersionId) : Map.of(),
                Map.of("targetVersionId", targetVersionId, "snapshotVersionId", snapshot.getId()));

        return diseaseMapper.toDiseaseVersionDTO(snapshot);
    }

    @Override
    public void validateTransition(VersionStatus from, VersionStatus to) {
        validateWorkflowTransition(from, to);
    }

    // ===================================
    // HELPER METHODS
    // ===================================

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

    private void validateWorkflowTransition(VersionStatus currentStatus, VersionStatus targetStatus) {
        if (!currentStatus.canTransitionTo(targetStatus)) {
            throw new IllegalStateException("Invalid workflow transition: " + currentStatus + " -> " + targetStatus);
        }
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
