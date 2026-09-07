package com.duoq.medlearn.knowledge.disease.service;

import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import com.duoq.medlearn.knowledge.version.dto.request.ModerationRequest;
import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;

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