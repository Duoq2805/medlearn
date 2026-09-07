package com.duoq.medlearn.flashcard.service.impl;

import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.flashcard.dto.response.FlashcardProgressResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardReviewResponse;
import com.duoq.medlearn.flashcard.entity.Flashcard;
import com.duoq.medlearn.flashcard.entity.FlashcardProgress;
import com.duoq.medlearn.flashcard.entity.FlashcardReviewLog;
import com.duoq.medlearn.flashcard.enums.FlashcardStatus;
import com.duoq.medlearn.flashcard.mapper.FlashcardMapper;
import com.duoq.medlearn.flashcard.repository.FlashcardProgressRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardReviewLogRepository;
import com.duoq.medlearn.flashcard.service.FlashcardReviewService;
import com.duoq.medlearn.flashcard.sm2.Sm2Algorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardReviewServiceImpl implements FlashcardReviewService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardProgressRepository progressRepository;
    private final FlashcardReviewLogRepository reviewLogRepository;
    private final FlashcardMapper flashcardMapper;

    @Override
    @Transactional(readOnly = true)
    public List<FlashcardProgressResponse> getDueCards(Long userId, int limit) {
        var now = OffsetDateTime.now();
        var pageable = PageRequest.of(0, Math.min(limit, 100));
        var due = progressRepository.findDueByUserId(userId, now, pageable);

        return due.stream()
                .map(this::toProgressResponse)
                .toList();
    }

    @Override
    @Transactional
    public FlashcardReviewResponse review(Long flashcardId, int quality, Integer responseTimeMs, Long userId) {
        var card = flashcardRepository.findByIdAndDeletedAtIsNull(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found: " + flashcardId));

        if (card.getStatus() != FlashcardStatus.ACTIVE) {
            throw new IllegalStateException("Cannot review a non-active flashcard");
        }

        // Retry loop for optimistic locking
        int retries = 3;
        while (true) {
            try {
                return doReview(card, quality, responseTimeMs, userId);
            } catch (ObjectOptimisticLockingFailureException e) {
                retries--;
                if (retries <= 0) {
                    throw e;
                }
                log.debug("Optimistic lock retry for flashcard={}, userId={}", flashcardId, userId);
            }
        }
    }

    private FlashcardReviewResponse doReview(Flashcard card, int quality, Integer responseTimeMs, Long userId) {
        var now = OffsetDateTime.now();

        var progress = progressRepository
                .findByFlashcardIdAndUserIdAndDeletedAtIsNull(card.getId(), userId)
                .orElseGet(() -> FlashcardProgress.builder()
                        .flashcard(card)
                        .user(User.builder().id(userId).build())
                        .build());

        var efBefore = progress.getEasinessFactor();
        var intervalBefore = progress.getInterval();

        var result = Sm2Algorithm.calculate(quality, progress.getEasinessFactor(),
                progress.getInterval(), progress.getRepetitions());

        // Update progress
        progress.setEasinessFactor(result.getEasinessFactor());
        progress.setInterval(result.getInterval());
        progress.setRepetitions(result.getRepetitions());
        progress.setNextReviewAt(result.getNextReviewAt());
        progress.setLastReviewedAt(now);
        progress.setLastQuality(quality);
        progress.setTotalReviews(progress.getTotalReviews() + 1);

        if (Sm2Algorithm.isCorrect(quality)) {
            progress.setCorrectCount(progress.getCorrectCount() + 1);
        } else {
            progress.setIncorrectCount(progress.getIncorrectCount() + 1);
        }

        if (quality >= 4 && progress.getRepetitions() >= 5) {
            progress.setMastered(true);
        }

        progressRepository.save(progress);

        // Update card aggregate counters
        card.setTotalReviews(card.getTotalReviews() + 1);
        if (Sm2Algorithm.isCorrect(quality)) {
            card.setCorrectCount(card.getCorrectCount() + 1);
        } else {
            card.setIncorrectCount(card.getIncorrectCount() + 1);
        }
        flashcardRepository.save(card);

        // Log review
        var logEntry = FlashcardReviewLog.builder()
                .flashcard(card)
                .user(User.builder().id(userId).build())
                .quality(quality)
                .responseTimeMs(responseTimeMs)
                .reviewedAt(now)
                .easinessFactorBefore(efBefore)
                .easinessFactorAfter(result.getEasinessFactor())
                .intervalBefore(intervalBefore)
                .intervalAfter(result.getInterval())
                .build();
        reviewLogRepository.save(logEntry);

        log.debug("Flashcard reviewed: id={}, quality={}, ef={}, interval={}, reps={}",
                card.getId(), quality, result.getEasinessFactor(), result.getInterval(), result.getRepetitions());

        return FlashcardReviewResponse.builder()
                .flashcardId(card.getId())
                .quality(quality)
                .easinessFactor(result.getEasinessFactor())
                .interval(result.getInterval())
                .repetitions(result.getRepetitions())
                .nextReviewAt(result.getNextReviewAt())
                .totalReviews(progress.getTotalReviews())
                .mastered(progress.isMastered())
                .build();
    }

    private FlashcardProgressResponse toProgressResponse(FlashcardProgress fp) {
        return FlashcardProgressResponse.builder()
                .flashcard(flashcardMapper.toResponse(fp.getFlashcard()))
                .easinessFactor(fp.getEasinessFactor())
                .interval(fp.getInterval())
                .repetitions(fp.getRepetitions())
                .totalReviews(fp.getTotalReviews())
                .correctCount(fp.getCorrectCount())
                .incorrectCount(fp.getIncorrectCount())
                .nextReviewAt(fp.getNextReviewAt())
                .lastReviewedAt(fp.getLastReviewedAt())
                .lastQuality(fp.getLastQuality())
                .mastered(fp.isMastered())
                .build();
    }
}
