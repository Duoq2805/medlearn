package com.duoq.medlearn.flashcard.dto.request;

import com.duoq.medlearn.flashcard.enums.FlashcardStatus;
import jakarta.validation.constraints.NotEmpty;
import lombok.Value;

import java.util.List;

@Value
public class FlashcardBatchUpdateRequest {

    @NotEmpty
    List<Long> ids;

    FlashcardStatus status;
}
