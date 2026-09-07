package com.duoq.medlearn.flashcard.dto.response;

import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder(toBuilder = true)
public class FlashcardProgressResponse {
    FlashcardResponse flashcard;
    double easinessFactor;
    int interval;
    int repetitions;
    int totalReviews;
    int correctCount;
    int incorrectCount;
    OffsetDateTime nextReviewAt;
    OffsetDateTime lastReviewedAt;
    Integer lastQuality;
    boolean mastered;
}
