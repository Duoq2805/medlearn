package com.duoq.medlearn.flashcard.controller;

import com.duoq.medlearn.common.dto.ApiResponse;
import com.duoq.medlearn.common.dto.PagedResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardDeckResponse;
import com.duoq.medlearn.flashcard.service.FlashcardService;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flashcards/decks")
@RequiredArgsConstructor
@Tag(name = "Flashcard Decks", description = "Flashcard deck management")
public class FlashcardDeckController {

    private final FlashcardService flashcardService;
    private final CurrentUserResolver currentUserResolver;

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_VIEW')")
    @Operation(summary = "List flashcard decks")
    public ResponseEntity<ApiResponse<PagedResponse<FlashcardDeckResponse>>> listDecks(
            @RequestParam(defaultValue = "false") boolean admin,
            Pageable pageable) {
        var userId = currentUserResolver.resolveCurrentUserId();
        var page = flashcardService.listDecks(userId, admin, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }
}
