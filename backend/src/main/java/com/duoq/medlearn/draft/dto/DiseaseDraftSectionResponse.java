package com.duoq.medlearn.draft.dto;

import com.duoq.medlearn.draft.enums.DraftSectionType;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class DiseaseDraftSectionResponse {
    Long id;
    DraftSectionType sectionType;
    String title;
    String content;
    Integer orderIndex;
    Integer wordCount;
    Boolean aiGenerated;
}
