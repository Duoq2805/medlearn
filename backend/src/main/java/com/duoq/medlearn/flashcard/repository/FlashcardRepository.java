package com.duoq.medlearn.flashcard.repository;

import com.duoq.medlearn.flashcard.entity.Flashcard;
import com.duoq.medlearn.flashcard.enums.FlashcardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardRepository extends JpaRepository<Flashcard, Long> {

    Page<Flashcard> findAllByDeckIdAndDeletedAtIsNull(Long deckId, Pageable pageable);

    List<Flashcard> findAllByDeckIdAndDeletedAtIsNull(Long deckId);

    Optional<Flashcard> findByIdAndDeletedAtIsNull(Long id);

    long countByCreatedByIdAndDeletedAtIsNull(Long userId);

    long countByDeckIdAndDeletedAtIsNull(Long deckId);

    long countByDeckIdAndStatusAndDeletedAtIsNull(Long deckId, FlashcardStatus status);

    @Query("SELECT f FROM Flashcard f WHERE f.deck.id IN :deckIds AND f.deletedAt IS NULL")
    List<Flashcard> findAllByDeckIds(@Param("deckIds") Collection<Long> deckIds);

    Page<Flashcard> findAllByCreatedByIdAndDeletedAtIsNull(Long userId, Pageable pageable);
}
