package com.duoq.medlearn.flashcard.repository;

import com.duoq.medlearn.flashcard.entity.FlashcardDeck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FlashcardDeckRepository extends JpaRepository<FlashcardDeck, Long> {

    Page<FlashcardDeck> findAllByCreatedByIdAndDeletedAtIsNull(Long userId, Pageable pageable);

    Page<FlashcardDeck> findAllByDeletedAtIsNull(Pageable pageable);

    Optional<FlashcardDeck> findByIdAndDeletedAtIsNull(Long id);

    long countByCreatedByIdAndDeletedAtIsNull(Long userId);
}
