package com.duoq.medlearn.draft.dto;

import com.duoq.medlearn.draft.enums.DraftSectionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.util.List;

@Value
public class UpdateDraftRequest {
    String title;

    @NotNull
    List<SectionContent> sections;

    @Value
    public static class SectionContent {
        Long id;
        @NotNull
        DraftSectionType sectionType;
        @NotBlank
        String title;
        @NotBlank
        String content;
        Integer orderIndex;
    }
}
