package com.duoq.medlearn.flashcard.service;

import com.duoq.medlearn.flashcard.entity.Flashcard;
import com.duoq.medlearn.flashcard.entity.FlashcardDeck;
import com.duoq.medlearn.flashcard.entity.FlashcardProgress;
import com.duoq.medlearn.flashcard.enums.SourceType;
import com.duoq.medlearn.flashcard.repository.FlashcardDeckRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardProgressRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardReviewLogRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class FlashcardIntegrationTest {

    @Autowired private TestEntityManager em;
    @Autowired private FlashcardDeckRepository deckRepository;
    @Autowired private FlashcardRepository flashcardRepository;
    @Autowired private FlashcardProgressRepository progressRepository;
    @Autowired private FlashcardReviewLogRepository reviewLogRepository;

    @Test
    void createAndReviewCard_ShouldPersistProgress() {
        var userRef = em.persist(com.duoq.medlearn.auth.entity.User.builder()
                .username("flashcard-test").email("fl@test.com")
                .passwordHash("hash").fullName("Test").build());

        var deck = deckRepository.save(FlashcardDeck.builder()
                .title("Integration Test Deck")
                .sourceType(SourceType.DISEASE).sourceId(1L)
                .createdBy(userRef).build());

        var card = flashcardRepository.save(Flashcard.builder()
                .deck(deck).question("Test Q?").answer("Test A")
                .createdBy(userRef).build());

        var progress = progressRepository.save(FlashcardProgress.builder()
                .flashcard(card).user(userRef)
                .easinessFactor(2.5).interval(0).repetitions(0)
                .build());

        assertNotNull(progress.getId());
        assertEquals(2.5, progress.getEasinessFactor());
        assertEquals(0, progress.getInterval());

        var found = flashcardRepository.findByIdAndDeletedAtIsNull(card.getId());
        assertTrue(found.isPresent());
        assertEquals("Test Q?", found.get().getQuestion());
    }

    @Test
    void softDelete_ShouldHideFromQueries() {
        var userRef = em.persist(com.duoq.medlearn.auth.entity.User.builder()
                .username("fl-del").email("fldel@test.com")
                .passwordHash("hash").fullName("Test").build());

        var deck = deckRepository.save(FlashcardDeck.builder()
                .title("Delete Test").sourceType(SourceType.DISEASE).sourceId(2L)
                .createdBy(userRef).build());

        var card = flashcardRepository.save(Flashcard.builder().deck(deck)
                .question("Q?").answer("A").createdBy(userRef).build());

        // Soft delete
        em.persist(deck); deck.setDeletedAt(java.time.OffsetDateTime.now());
        em.persist(card); card.setDeletedAt(java.time.OffsetDateTime.now());

        var found = flashcardRepository.findByIdAndDeletedAtIsNull(card.getId());
        assertFalse(found.isPresent());
    }
}
