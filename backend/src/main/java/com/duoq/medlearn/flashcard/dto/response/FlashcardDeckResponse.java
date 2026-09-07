package com.duoq.medlearn.flashcard.dto.response;

import com.duoq.medlearn.flashcard.enums.SourceType;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder(toBuilder = true)
public class FlashcardDeckResponse {
    Long id;
    String title;
    String description;
    SourceType sourceType;
    Long sourceId;
    String status;
    Integer cardCount;
    Long createdBy;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
