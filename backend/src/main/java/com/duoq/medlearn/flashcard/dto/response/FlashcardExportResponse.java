package com.duoq.medlearn.flashcard.dto.response;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class FlashcardExportResponse {
    String format;
    String deckTitle;
    int count;
    List<FlashcardResponse> flashcards;
    String csvData;
}
