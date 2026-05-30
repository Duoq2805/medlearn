package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.enums.VersionStatus;
import com.duoq.medlearn.dto.request.ModerationRequest;
import com.duoq.medlearn.dto.response.DiseaseVersionDTO;

public interface DiseaseWorkflowService {

    DiseaseVersionDTO submit(Long versionId);

    DiseaseVersionDTO approve(
            Long versionId,
            ModerationRequest request
    );

    DiseaseVersionDTO reject(
            Long versionId,
            ModerationRequest request
    );

    DiseaseVersionDTO rollback(
            Long diseaseId,
            Long targetVersionId
    );

    void validateTransition(
            VersionStatus from,
            VersionStatus to
    );
}