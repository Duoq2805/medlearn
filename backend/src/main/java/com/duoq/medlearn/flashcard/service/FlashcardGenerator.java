package com.duoq.medlearn.flashcard.service;

import com.duoq.medlearn.flashcard.dto.request.FlashcardGenerateRequest;
import com.duoq.medlearn.flashcard.dto.response.FlashcardGenerateResponse;

public interface FlashcardGenerator {

    FlashcardGenerateResponse generate(FlashcardGenerateRequest request, Long userId);
}
