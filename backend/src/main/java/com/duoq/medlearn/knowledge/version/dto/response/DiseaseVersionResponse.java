package com.duoq.medlearn.knowledge.version.dto.response;

import com.duoq.medlearn.knowledge.version.enums.VersionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

@Data
@Builder
public class DiseaseVersionResponse {
    private Long id;
    private Long diseaseId;
    private Integer versionNumber;
    private VersionStatus status;
    private String moderationNote;
    private Long createdById;
    private Long reviewedById;
    private OffsetDateTime reviewedAt;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
