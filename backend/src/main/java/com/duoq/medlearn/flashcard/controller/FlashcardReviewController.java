package com.duoq.medlearn.flashcard.controller;

import com.duoq.medlearn.common.dto.ApiResponse;
import com.duoq.medlearn.flashcard.dto.request.FlashcardReviewRequest;
import com.duoq.medlearn.flashcard.dto.response.FlashcardProgressResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardReviewResponse;
import com.duoq.medlearn.flashcard.service.FlashcardReviewService;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@Tag(name = "Flashcard Review", description = "Flashcard review using SM-2 spaced repetition")
public class FlashcardReviewController {

    private final FlashcardReviewService reviewService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping("/due")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_REVIEW')")
    @Operation(summary = "Get flashcards due for review")
    public ResponseEntity<ApiResponse<List<FlashcardProgressResponse>>> getDue(
            @RequestParam(defaultValue = "20") int limit) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(reviewService.getDueCards(userId, limit)));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_REVIEW')")
    @Operation(summary = "Submit a flashcard review with SM-2 quality rating")
    public ResponseEntity<ApiResponse<FlashcardReviewResponse>> review(
            @PathVariable Long id,
            @Valid @RequestBody FlashcardReviewRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        var response = reviewService.review(id, request.getQuality(), request.getResponseTimeMs(), userId);
        return ResponseEntity.ok(ApiResponse.success("Review recorded", response));
    }
}
