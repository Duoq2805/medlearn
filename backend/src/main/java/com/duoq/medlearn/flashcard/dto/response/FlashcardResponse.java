package com.duoq.medlearn.flashcard.dto.response;

import com.duoq.medlearn.flashcard.enums.FlashcardDifficulty;
import com.duoq.medlearn.flashcard.enums.FlashcardStatus;
import lombok.Builder;
import lombok.Value;

import java.time.OffsetDateTime;

@Value
@Builder(toBuilder = true)
public class FlashcardResponse {
    Long id;
    Long deckId;
    String deckTitle;
    String question;
    String answer;
    String explanation;
    String source;
    String tag;
    FlashcardDifficulty difficulty;
    FlashcardStatus status;
    Long createdBy;
    int totalReviews;
    int correctCount;
    int incorrectCount;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
