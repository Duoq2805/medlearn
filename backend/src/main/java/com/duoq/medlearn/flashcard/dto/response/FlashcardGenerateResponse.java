package com.duoq.medlearn.flashcard.dto.response;

import com.duoq.medlearn.ai.dto.response.AiUsage;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder(toBuilder = true)
public class FlashcardGenerateResponse {
    String generationId;
    int totalGenerated;
    List<FlashcardResponse> flashcards;
    AiUsage usage;
}
