package com.duoq.medlearn.flashcard.dto.response;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class FlashcardStatsResponse {
    long totalCards;
    long masteredCards;
    long cardsDueToday;
    long cardsDueThisWeek;
    double averageEasinessFactor;
    double overallAccuracy;
    long totalReviews;
    long totalDecks;
}
