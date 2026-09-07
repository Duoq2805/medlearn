package com.duoq.medlearn.flashcard.repository;

import com.duoq.medlearn.flashcard.entity.FlashcardProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardProgressRepository extends JpaRepository<FlashcardProgress, Long> {

    Optional<FlashcardProgress> findByFlashcardIdAndUserIdAndDeletedAtIsNull(Long flashcardId, Long userId);

    @Query("SELECT fp FROM FlashcardProgress fp " +
           "JOIN FETCH fp.flashcard f " +
           "JOIN FETCH f.createdBy " +
           "JOIN FETCH f.deck " +
           "WHERE fp.user.id = :userId " +
           "AND fp.deletedAt IS NULL " +
           "AND f.deletedAt IS NULL " +
           "AND f.status = 'ACTIVE' " +
           "AND fp.nextReviewAt <= :now " +
           "ORDER BY fp.nextReviewAt ASC")
    List<FlashcardProgress> findDueByUserId(@Param("userId") Long userId, @Param("now") OffsetDateTime now, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(fp) FROM FlashcardProgress fp " +
           "JOIN fp.flashcard f " +
           "WHERE fp.user.id = :userId " +
           "AND fp.deletedAt IS NULL " +
           "AND f.deletedAt IS NULL " +
           "AND f.status = 'ACTIVE' " +
           "AND fp.nextReviewAt <= :now")
    long countDueByUserId(@Param("userId") Long userId, @Param("now") OffsetDateTime now);

    long countByUserIdAndDeletedAtIsNull(Long userId);

    long countByUserIdAndMasteredTrueAndDeletedAtIsNull(Long userId);

    @Query("SELECT AVG(fp.easinessFactor) FROM FlashcardProgress fp WHERE fp.user.id = :userId AND fp.deletedAt IS NULL")
    Double averageEasinessFactorByUserId(@Param("userId") Long userId);
}
