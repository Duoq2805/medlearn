package com.duoq.medlearn.flashcard.service.impl;

import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.flashcard.dto.request.FlashcardBatchUpdateRequest;
import com.duoq.medlearn.flashcard.dto.request.FlashcardUpdateRequest;
import com.duoq.medlearn.flashcard.dto.response.*;
import com.duoq.medlearn.flashcard.entity.Flashcard;
import com.duoq.medlearn.flashcard.entity.FlashcardDeck;
import com.duoq.medlearn.flashcard.enums.FlashcardStatus;
import com.duoq.medlearn.flashcard.mapper.FlashcardMapper;
import com.duoq.medlearn.flashcard.repository.FlashcardDeckRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardProgressRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardRepository;
import com.duoq.medlearn.flashcard.repository.FlashcardReviewLogRepository;
import com.duoq.medlearn.flashcard.service.FlashcardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardServiceImpl implements FlashcardService {

    private final FlashcardRepository flashcardRepository;
    private final FlashcardDeckRepository deckRepository;
    private final FlashcardProgressRepository progressRepository;
    private final FlashcardReviewLogRepository reviewLogRepository;
    private final FlashcardMapper flashcardMapper;

    @Override
    @Transactional(readOnly = true)
    public FlashcardResponse getById(Long id) {
        var card = flashcardRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found: " + id));
        return flashcardMapper.toResponse(card);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlashcardDeckResponse> listDecks(Long userId, boolean admin, Pageable pageable) {
        if (admin) {
            return deckRepository.findAllByDeletedAtIsNull(pageable)
                    .map(flashcardMapper::toDeckResponse);
        }
        return deckRepository.findAllByCreatedByIdAndDeletedAtIsNull(userId, pageable)
                .map(flashcardMapper::toDeckResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlashcardResponse> listByDeck(Long deckId, Pageable pageable) {
        var deck = deckRepository.findByIdAndDeletedAtIsNull(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + deckId));
        return flashcardRepository.findAllByDeckIdAndDeletedAtIsNull(deck.getId(), pageable)
                .map(flashcardMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FlashcardResponse> listByUser(Long userId, Pageable pageable) {
        return flashcardRepository.findAllByCreatedByIdAndDeletedAtIsNull(userId, pageable)
                .map(flashcardMapper::toResponse);
    }

    @Override
    @Transactional
    public FlashcardResponse update(Long id, FlashcardUpdateRequest request, Long userId) {
        var card = flashcardRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found: " + id));

        if (!card.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot edit another user's flashcard");
        }

        if (request.getQuestion() != null) card.setQuestion(request.getQuestion());
        if (request.getAnswer() != null) card.setAnswer(request.getAnswer());
        if (request.getExplanation() != null) card.setExplanation(request.getExplanation());
        if (request.getTag() != null) card.setTag(request.getTag());
        if (request.getDifficulty() != null) card.setDifficulty(request.getDifficulty());
        if (request.getStatus() != null) card.setStatus(request.getStatus());

        var saved = flashcardRepository.save(card);
        log.info("Flashcard updated: id={}, userId={}", id, userId);
        return flashcardMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        var card = flashcardRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard not found: " + id));

        if (!card.getCreatedBy().getId().equals(userId)) {
            throw new IllegalArgumentException("Cannot delete another user's flashcard");
        }

        card.setDeletedAt(OffsetDateTime.now());
        flashcardRepository.save(card);
        log.info("Flashcard soft deleted: id={}, userId={}", id, userId);
    }

    @Override
    @Transactional
    public void batchUpdate(FlashcardBatchUpdateRequest request, Long userId) {
        var cards = flashcardRepository.findAllByDeckIds(request.getIds());
        var toUpdate = cards.stream()
                .filter(c -> c.getCreatedBy().getId().equals(userId))
                .peek(c -> c.setStatus(request.getStatus()))
                .toList();
        flashcardRepository.saveAll(toUpdate);
        log.info("Batch update: {} cards status={}", toUpdate.size(), request.getStatus());
    }

    @Override
    @Transactional(readOnly = true)
    public FlashcardExportResponse exportDeck(Long deckId, String format, Long userId) {
        var deck = deckRepository.findByIdAndDeletedAtIsNull(deckId)
                .orElseThrow(() -> new ResourceNotFoundException("Deck not found: " + deckId));
        if (!deck.getCreatedBy().getId().equals(userId)) {
            // ponytail: check FLASHCARD_MANAGE permission for admin bypass; add when permission service exposes hasPermission in service layer
            throw new IllegalArgumentException("Cannot export another user's deck");
        }
        var cards = flashcardRepository.findAllByDeckIdAndDeletedAtIsNull(deckId);

        var responses = cards.stream()
                .map(flashcardMapper::toResponse)
                .toList();

        if ("csv".equalsIgnoreCase(format)) {
            var csv = buildCsv(responses);
            return FlashcardExportResponse.builder()
                    .format("csv")
                    .deckTitle(deck.getTitle())
                    .count(responses.size())
                    .csvData(csv)
                    .build();
        }

        return FlashcardExportResponse.builder()
                .format("json")
                .deckTitle(deck.getTitle())
                .count(responses.size())
                .flashcards(responses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FlashcardStatsResponse getStats(Long userId) {
        var now = OffsetDateTime.now();
        var totalCards = flashcardRepository.countByCreatedByIdAndDeletedAtIsNull(userId);
        var dueNow = progressRepository.countDueByUserId(userId, now);
        var dueWeek = progressRepository.countDueByUserId(userId, now.plusDays(7));
        var avgEf = progressRepository.averageEasinessFactorByUserId(userId);
        var totalReviews = reviewLogRepository.countByUserId(userId);
        var correctReviews = reviewLogRepository.countCorrectByUserId(userId);
        var totalDecks = deckRepository.countByCreatedByIdAndDeletedAtIsNull(userId);
        var mastered = progressRepository.countByUserIdAndMasteredTrueAndDeletedAtIsNull(userId);

        return FlashcardStatsResponse.builder()
                .totalCards(totalCards)
                .masteredCards(mastered)
                .cardsDueToday(dueNow)
                .cardsDueThisWeek(dueWeek)
                .averageEasinessFactor(avgEf != null ? avgEf : 2.5)
                .overallAccuracy(totalReviews > 0 ? (double) correctReviews / totalReviews * 100 : 0)
                .totalReviews(totalReviews)
                .totalDecks(totalDecks)
                .build();
    }

    private String buildCsv(List<FlashcardResponse> cards) {
        var sb = new StringBuilder();
        sb.append("question,answer,explanation,tag,difficulty\n");
        for (var c : cards) {
            sb.append(escapeCsv(c.getQuestion())).append(",");
            sb.append(escapeCsv(c.getAnswer())).append(",");
            sb.append(escapeCsv(c.getExplanation())).append(",");
            sb.append(escapeCsv(c.getTag())).append(",");
            sb.append(c.getDifficulty()).append("\n");
        }
        return sb.toString();
    }

    private String escapeCsv(String value) {
        if (value == null) return "\"\"";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}
