package com.duoq.medlearn.draft.dto;

import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftStatus;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;
import java.util.List;

@Value
@Builder(toBuilder = true)
public class DiseaseDraftResponse {
    Long id;
    Long diseaseId;
    String diseaseName;
    String title;
    DraftStatus status;
    DraftMethod sourceMethod;
    Long sourceDocumentId;
    String aiModel;
    Integer aiTotalTokens;
    Integer aiLatencyMs;
    String reviewNote;
    Long createdBy;
    Long reviewedBy;
    OffsetDateTime reviewedAt;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
    List<DiseaseDraftSectionResponse> sections;
}
