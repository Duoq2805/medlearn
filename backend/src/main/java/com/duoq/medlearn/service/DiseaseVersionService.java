package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.enums.VersionStatus;
import com.duoq.medlearn.dto.request.CreateDiseaseVersionRequest;
import com.duoq.medlearn.dto.request.ModerationRequest;
import com.duoq.medlearn.dto.request.UpdateDiseaseVersionRequest;
import com.duoq.medlearn.dto.response.DiseaseVersionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiseaseVersionService {

    DiseaseVersionDTO createDraftVersion(Long diseaseId, CreateDiseaseVersionRequest request);

    DiseaseVersionDTO cloneApprovedVersion(Long diseaseId);

    DiseaseVersionDTO getVersionById(Long versionId);

    List<DiseaseVersionDTO> getDiseaseVersions(Long diseaseId);

    DiseaseVersionDTO getCurrentApprovedVersion(Long diseaseId);

    DiseaseVersionDTO getLatestDraftVersion(Long diseaseId);

    Page<DiseaseVersionDTO> getPendingReviewVersions(Pageable pageable);

    DiseaseVersionDTO updateDraftVersion(Long versionId, UpdateDiseaseVersionRequest request);

    DiseaseVersionDTO submitForReview(Long versionId);

    DiseaseVersionDTO approveVersion(Long versionId, ModerationRequest request);

    DiseaseVersionDTO rejectVersion(Long versionId, ModerationRequest request);

    DiseaseVersionDTO archiveVersion(Long versionId);

    void validateWorkflowTransition(VersionStatus currentStatus, VersionStatus targetStatus);

    boolean canEditVersion(Long versionId);

    boolean isApprovedVersion(Long versionId);

    boolean isPendingReview(Long versionId);

    void validateVersionOwnership(Long versionId);

    void validateReviewerPermission();

    Integer generateNextVersionNumber(Long diseaseId);

    void deactivatePreviousApprovedVersion(Long diseaseId);

    void softDeleteVersion(Long versionId);

    void restoreVersion(Long versionId);
}
