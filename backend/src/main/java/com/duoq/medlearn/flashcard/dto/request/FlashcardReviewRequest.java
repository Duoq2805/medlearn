package com.duoq.medlearn.flashcard.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

@Value
public class FlashcardReviewRequest {

    @NotNull
    @Min(0) @Max(5)
    Integer quality;

    @Min(0) @Max(120000)
    Integer responseTimeMs;
}
