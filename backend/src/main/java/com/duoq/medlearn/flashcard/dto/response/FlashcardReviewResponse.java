package com.duoq.medlearn.flashcard.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder(toBuilder = true)
public class FlashcardReviewResponse {
    Long flashcardId;
    int quality;
    double easinessFactor;
    int interval;
    int repetitions;
    OffsetDateTime nextReviewAt;
    int totalReviews;
    boolean mastered;
}
