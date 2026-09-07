package com.duoq.medlearn.flashcard.service;

import com.duoq.medlearn.flashcard.dto.response.FlashcardProgressResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardReviewResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FlashcardReviewService {

    List<FlashcardProgressResponse> getDueCards(Long userId, int limit);

    FlashcardReviewResponse review(Long flashcardId, int quality, Integer responseTimeMs, Long userId);
}
