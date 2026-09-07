package com.duoq.medlearn.draft.dto.request;

import com.duoq.medlearn.draft.enums.DraftMethod;
import com.duoq.medlearn.draft.enums.DraftSectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.List;

@Value
public class CreateDraftRequest {
    Long diseaseId;

    @NotBlank
    String title;

    DraftMethod sourceMethod;

    Long sourceDocumentId;

    @NotNull
    List<SectionContent> sections;

    @Value
    public static class SectionContent {
        @NotNull
        DraftSectionType sectionType;
        @NotBlank
        String title;
        @NotBlank
        String content;
        Integer orderIndex;
    }
}
