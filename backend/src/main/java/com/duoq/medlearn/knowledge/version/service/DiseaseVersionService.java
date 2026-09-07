package com.duoq.medlearn.knowledge.version.service;

import com.duoq.medlearn.knowledge.version.dto.request.CreateDiseaseVersionRequest;
import com.duoq.medlearn.knowledge.version.dto.request.ModerationRequest;
import com.duoq.medlearn.knowledge.version.dto.request.UpdateDiseaseVersionRequest;
import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DiseaseVersionService {

    DiseaseVersionResponse createDraftVersion(Long diseaseId, CreateDiseaseVersionRequest request);

    DiseaseVersionResponse cloneApprovedVersion(Long diseaseId);

    DiseaseVersionResponse getVersionById(Long versionId);

    List<DiseaseVersionResponse> getDiseaseVersions(Long diseaseId);

    DiseaseVersionResponse getCurrentApprovedVersion(Long diseaseId);

    DiseaseVersionResponse getLatestDraftVersion(Long diseaseId);

    Page<DiseaseVersionResponse> getPendingReviewVersions(Pageable pageable);

    DiseaseVersionResponse updateDraftVersion(Long versionId, UpdateDiseaseVersionRequest request);

    DiseaseVersionResponse submitForReview(Long versionId);

    DiseaseVersionResponse approveVersion(Long versionId, ModerationRequest request);

    DiseaseVersionResponse rejectVersion(Long versionId, ModerationRequest request);

    DiseaseVersionResponse archiveVersion(Long versionId);

    void softDeleteVersion(Long versionId);

    void restoreVersion(Long versionId);
}
