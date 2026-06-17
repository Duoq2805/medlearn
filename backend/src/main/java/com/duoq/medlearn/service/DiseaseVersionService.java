package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.version.CreateDiseaseVersionRequest;
import com.duoq.medlearn.domain.dto.version.ModerationRequest;
import com.duoq.medlearn.domain.dto.version.UpdateDiseaseVersionRequest;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionDTO;
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

    void softDeleteVersion(Long versionId);

    void restoreVersion(Long versionId);
}
