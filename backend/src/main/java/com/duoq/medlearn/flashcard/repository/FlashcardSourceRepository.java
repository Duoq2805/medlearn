package com.duoq.medlearn.flashcard.repository;

import com.duoq.medlearn.flashcard.entity.FlashcardSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlashcardSourceRepository extends JpaRepository<FlashcardSource, Long> {

    List<FlashcardSource> findAllByFlashcardId(Long flashcardId);

    List<FlashcardSource> findAllByFlashcardIdIn(List<Long> flashcardIds);

    List<FlashcardSource> findBySourceTypeAndSourceId(String sourceType, Long sourceId);
}
