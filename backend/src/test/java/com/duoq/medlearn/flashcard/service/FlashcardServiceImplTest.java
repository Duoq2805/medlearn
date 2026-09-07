package com.duoq.medlearn.flashcard.service;

import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.flashcard.dto.request.FlashcardUpdateRequest;
import com.duoq.medlearn.flashcard.dto.response.FlashcardResponse;
import com.duoq.medlearn.flashcard.entity.Flashcard;
import com.duoq.medlearn.flashcard.entity.FlashcardDeck;
import com.duoq.medlearn.flashcard.enums.FlashcardDifficulty;
import com.duoq.medlearn.flashcard.enums.SourceType;
import com.duoq.medlearn.flashcard.mapper.FlashcardMapper;
import com.duoq.medlearn.flashcard.repository.FlashcardDeckRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardProgressRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardReviewLogRepository;
import com.duoq.medlearn.flashcard.service.impl.FlashcardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceImplTest {

    @Mock private FlashcardRepository flashcardRepository;
    @Mock private FlashcardDeckRepository deckRepository;
    @Mock private FlashcardProgressRepository progressRepository;
    @Mock private FlashcardReviewLogRepository reviewLogRepository;
    @Mock private FlashcardMapper flashcardMapper;

    private FlashcardServiceImpl flashcardService;
    private Flashcard card;
    private User user;
    private FlashcardDeck deck;

    @BeforeEach
    void setUp() {
        flashcardService = new FlashcardServiceImpl(
                flashcardRepository, deckRepository, progressRepository, reviewLogRepository, flashcardMapper);

        user = User.builder().id(1L).build();
        deck = FlashcardDeck.builder()
                .id(1L).title("Test Deck")
                .sourceType(SourceType.DISEASE).sourceId(1L)
                .createdBy(user).build();
        card = Flashcard.builder()
                .id(1L).deck(deck)
                .question("Q?").answer("A")
                .createdBy(user).build();
    }

    @Test
    void getById_Success() {
        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));
        when(flashcardMapper.toResponse(card)).thenReturn(
                FlashcardResponse.builder().id(1L).question("Q?").answer("A").build());
        var response = flashcardService.getById(1L);
        assertEquals("Q?", response.getQuestion());
    }

    @Test
    void getById_NotFound() {
        when(flashcardRepository.findByIdAndDeletedAtIsNull(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> flashcardService.getById(99L));
    }

    @Test
    void update_Success() {
        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));
        when(flashcardRepository.save(any())).thenReturn(card);
        when(flashcardMapper.toResponse(any())).thenReturn(
                FlashcardResponse.builder().id(1L).question("New Q?").answer("New A").build());

        var request = new FlashcardUpdateRequest("New Q?", "New A", null, null, null, null);
        var response = flashcardService.update(1L, request, 1L);

        assertEquals("New Q?", response.getQuestion());
        assertEquals("New A", response.getAnswer());
    }

    @Test
    void update_OtherUser_ShouldThrow() {
        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));
        var req = new FlashcardUpdateRequest(null, null, null, null, null, null);
        assertThrows(IllegalArgumentException.class, () -> flashcardService.update(1L, req, 2L));
    }

    @Test
    void delete_Success() {
        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));
        flashcardService.delete(1L, 1L);
        verify(flashcardRepository).save(any());
    }

    @Test
    void delete_OtherUser_ShouldThrow() {
        when(flashcardRepository.findByIdAndDeletedAtIsNull(1L)).thenReturn(Optional.of(card));
        assertThrows(IllegalArgumentException.class, () -> flashcardService.delete(1L, 2L));
    }

    @Test
    void listDecks_ShouldReturnUserDecks() {
        when(deckRepository.findAllByCreatedByIdAndDeletedAtIsNull(any(), any()))
                .thenReturn(new PageImpl<>(List.of(deck)));
        var page = flashcardService.listDecks(1L, false, PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void listDecks_Admin_ShouldReturnAll() {
        when(deckRepository.findAllByDeletedAtIsNull(any()))
                .thenReturn(new PageImpl<>(List.of(deck)));
        var page = flashcardService.listDecks(null, true, PageRequest.of(0, 20));
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void getStats_Success() {
        when(flashcardRepository.countByCreatedByIdAndDeletedAtIsNull(any())).thenReturn(150L);
        when(progressRepository.countDueByUserId(any(), any())).thenReturn(5L);
        when(progressRepository.averageEasinessFactorByUserId(any())).thenReturn(2.4);
        when(reviewLogRepository.countByUserId(any())).thenReturn(100L);
        when(reviewLogRepository.countCorrectByUserId(any())).thenReturn(80L);
        when(deckRepository.countByCreatedByIdAndDeletedAtIsNull(any())).thenReturn(3L);
        when(progressRepository.countByUserIdAndMasteredTrueAndDeletedAtIsNull(any())).thenReturn(20L);

        var stats = flashcardService.getStats(1L);
        assertEquals(80.0, stats.getOverallAccuracy(), 0.001);
        assertEquals(2.4, stats.getAverageEasinessFactor(), 0.001);
        assertEquals(20, stats.getMasteredCards());
        assertEquals(3, stats.getTotalDecks());
    }
}
