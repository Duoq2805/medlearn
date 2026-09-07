package com.duoq.medlearn.flashcard.dto.request;

import com.duoq.medlearn.flashcard.enums.FlashcardDifficulty;
import com.duoq.medlearn.flashcard.enums.FlashcardStatus;
import lombok.Value;

@Value
public class FlashcardUpdateRequest {
    String question;
    String answer;
    String explanation;
    String tag;
    FlashcardDifficulty difficulty;
    FlashcardStatus status;
}
