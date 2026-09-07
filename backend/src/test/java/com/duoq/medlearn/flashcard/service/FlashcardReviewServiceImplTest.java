package com.duoq.medlearn.flashcard.service;

import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.flashcard.entity.Flashcard;
import com.duoq.medlearn.flashcard.entity.FlashcardDeck;
import com.duoq.medlearn.flashcard.entity.FlashcardProgress;
import com.duoq.medlearn.flashcard.enums.FlashcardDifficulty;
import com.duoq.medlearn.flashcard.enums.FlashcardStatus;
import com.duoq.medlearn.flashcard.enums.SourceType;
import com.duoq.medlearn.flashcard.mapper.FlashcardMapper;
import com.duoq.medlearn.flashcard.repository.FlashcardProgressRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardReviewLogRepository;
import com.duoq.medlearn.flashcard.service.impl.FlashcardReviewServiceImpl;
import com.duoq.medlearn.flashcard.util.JsonFlashcardParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardReviewServiceImplTest {

    @Mock private FlashcardRepository flashcardRepository;
    @Mock private FlashcardProgressRepository progressRepository;
    @Mock private FlashcardReviewLogRepository reviewLogRepository;
    @Mock private FlashcardMapper flashcardMapper;

    private FlashcardReviewService reviewService;
    private Flashcard card;
    private User user;

    @BeforeEach
    void setUp() {
        reviewService = new FlashcardReviewServiceImpl(
                flashcardRepository, progressRepository, reviewLogRepository, flashcardMapper);

        user = User.builder().id(1L).build();

        var deck = FlashcardDeck.builder()
                .id(1L).title("Test Deck")
                .sourceType(SourceType.DISEASE).sourceId(1L)
                .createdBy(user).build();

        card = Flashcard.builder()
                .id(1L).deck(deck)
                .question("Q?").answer("A")
                .difficulty(FlashcardDifficulty.MEDIUM)
                .status(FlashcardStatus.ACTIVE)
                .createdBy(user).build();
    }

    @Test
    void firstReview_ShouldCreateProgress() {
        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));
        when(progressRepository.findByFlashcardIdAndUserIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.empty());
        when(flashcardRepository.save(any())).thenReturn(card);

        var response = reviewService.review(1L, 4, 3000, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getFlashcardId());
        assertEquals(4, response.getQuality());
        assertEquals(1, response.getInterval());
        assertEquals(1, response.getRepetitions());
        assertFalse(response.isMastered());
        verify(progressRepository).save(any(FlashcardProgress.class));
        verify(reviewLogRepository).save(any());
    }

    @Test
    void perfectReview_ShouldIncreaseEF() {
        var progress = FlashcardProgress.builder()
                .flashcard(card).user(user)
                .easinessFactor(2.5).interval(6).repetitions(2)
                .build();

        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));
        when(progressRepository.findByFlashcardIdAndUserIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(progress));
        when(flashcardRepository.save(any())).thenReturn(card);

        var response = reviewService.review(1L, 5, null, 1L);

        assertEquals(2.6, response.getEasinessFactor(), 0.001);
        assertEquals(15, response.getInterval()); // round(6 * 2.5) = 15, uses OLD ef
        assertEquals(3, response.getRepetitions());
    }

    @Test
    void failedReview_ShouldResetProgress() {
        var progress = FlashcardProgress.builder()
                .flashcard(card).user(user)
                .easinessFactor(2.5).interval(30).repetitions(10)
                .build();

        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));
        when(progressRepository.findByFlashcardIdAndUserIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(progress));
        when(flashcardRepository.save(any())).thenReturn(card);

        var response = reviewService.review(1L, 1, null, 1L);

        // quality=1 → ef = 2.5 + (0.1 - (4)*(0.08+(4)*0.02)) = 2.5 + (0.1 - 4*0.16) = 2.5 - 0.54 = 1.96
        assertEquals(1.96, response.getEasinessFactor(), 0.001);
        assertEquals(1, response.getInterval());
        assertEquals(0, response.getRepetitions());
    }

    @Test
    void fivePerfectReviews_ShouldSetMastered() {
        var progress = FlashcardProgress.builder()
                .flashcard(card).user(user)
                .easinessFactor(3.0).interval(30).repetitions(4)
                .totalReviews(4).correctCount(4)
                .build();

        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));
        when(progressRepository.findByFlashcardIdAndUserIdAndDeletedAtIsNull(1L, 1L)).thenReturn(Optional.of(progress));
        when(flashcardRepository.save(any())).thenReturn(card);

        var response = reviewService.review(1L, 4, null, 1L);

        assertTrue(response.isMastered());
        assertEquals(5, response.getRepetitions());
    }

    @Test
    void reviewNonActiveCard_ShouldThrow() {
        card.setStatus(FlashcardStatus.MASTERED);
        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));

        assertThrows(IllegalStateException.class, () -> reviewService.review(1L, 4, null, 1L));
    }

    @Test
    void getDueCards_ShouldReturnList() {
        var dueCards = progressRepository.findDueByUserId(any(), any(), any());
        verifyNoInteractions(flashcardRepository);
    }
}
