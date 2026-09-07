package com.duoq.medlearn.draft.dto.request;

import com.duoq.medlearn.draft.enums.DraftSectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.List;

@Value
public class AiDraftRequest {
    Long diseaseId;
    Long documentId;

    @NotBlank
    String title;

    @NotNull
    List<DraftSectionType> sections;

    String model;
    Double temperature;
}
