package com.duoq.medlearn.flashcard.repository;

import com.duoq.medlearn.flashcard.entity.FlashcardReviewLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;

@Repository
public interface FlashcardReviewLogRepository extends JpaRepository<FlashcardReviewLog, Long> {

    long countByUserId(Long userId);

    @Query("SELECT COUNT(rl) FROM FlashcardReviewLog rl WHERE rl.user.id = :userId AND rl.quality >= 3")
    long countCorrectByUserId(@Param("userId") Long userId);

    @Query("SELECT COUNT(rl) FROM FlashcardReviewLog rl WHERE rl.flashcard.id = :flashcardId")
    long countByFlashcardId(@Param("flashcardId") Long flashcardId);

    long countByUserIdAndReviewedAtAfter(Long userId, OffsetDateTime after);
}
