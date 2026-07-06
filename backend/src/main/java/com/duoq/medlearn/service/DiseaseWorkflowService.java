package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.enums.VersionStatus;
import com.duoq.medlearn.domain.dto.version.ModerationRequest;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionResponse;

public interface DiseaseWorkflowService {

    DiseaseVersionResponse submit(Long versionId);

    DiseaseVersionResponse approve(
            Long versionId,
            ModerationRequest request
    );

    DiseaseVersionResponse reject(
            Long versionId,
            ModerationRequest request
    );

    DiseaseVersionResponse rollback(
            Long diseaseId,
            Long targetVersionId
    );

    void validateTransition(
            VersionStatus from,
            VersionStatus to
    );
}