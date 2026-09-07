package com.duoq.medlearn.flashcard.service;

import com.duoq.medlearn.flashcard.dto.request.FlashcardBatchUpdateRequest;
import com.duoq.medlearn.flashcard.dto.request.FlashcardUpdateRequest;
import com.duoq.medlearn.flashcard.dto.response.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FlashcardService {

    FlashcardResponse getById(Long id);

    Page<FlashcardDeckResponse> listDecks(Long userId, boolean admin, Pageable pageable);

    Page<FlashcardResponse> listByDeck(Long deckId, Pageable pageable);

    Page<FlashcardResponse> listByUser(Long userId, Pageable pageable);

    FlashcardResponse update(Long id, FlashcardUpdateRequest request, Long userId);

    void delete(Long id, Long userId);

    void batchUpdate(FlashcardBatchUpdateRequest request, Long userId);

    FlashcardExportResponse exportDeck(Long deckId, String format, Long userId);

    FlashcardStatsResponse getStats(Long userId);
}
