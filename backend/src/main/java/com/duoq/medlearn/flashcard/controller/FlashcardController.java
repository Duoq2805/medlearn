package com.duoq.medlearn.flashcard.controller;

import com.duoq.medlearn.common.dto.ApiResponse;
import com.duoq.medlearn.common.dto.PagedResponse;
import com.duoq.medlearn.flashcard.dto.request.FlashcardBatchUpdateRequest;
import com.duoq.medlearn.flashcard.dto.request.FlashcardGenerateRequest;
import com.duoq.medlearn.flashcard.dto.request.FlashcardUpdateRequest;
import com.duoq.medlearn.flashcard.dto.response.FlashcardExportResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardGenerateResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardResponse;
import com.duoq.medlearn.flashcard.dto.response.FlashcardStatsResponse;
import com.duoq.medlearn.flashcard.service.FlashcardGenerator;
import com.duoq.medlearn.flashcard.service.FlashcardService;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flashcards")
@RequiredArgsConstructor
@Tag(name = "Flashcards", description = "AI-powered flashcard generation and management")
public class FlashcardController {

    private final FlashcardGenerator flashcardGenerator;
    private final FlashcardService flashcardService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/generate")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_GENERATE')")
    @Operation(summary = "Generate flashcards from a disease or document")
    public ResponseEntity<ApiResponse<FlashcardGenerateResponse>> generate(
            @Valid @RequestBody FlashcardGenerateRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        var response = flashcardGenerator.generate(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Generated " + response.getTotalGenerated() + " flashcards", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_VIEW')")
    @Operation(summary = "Get flashcard by ID")
    public ResponseEntity<ApiResponse<FlashcardResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(flashcardService.getById(id)));
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_VIEW')")
    @Operation(summary = "List flashcards by user")
    public ResponseEntity<ApiResponse<PagedResponse<FlashcardResponse>>> listByUser(Pageable pageable) {
        var userId = currentUserResolver.resolveCurrentUserId();
        var page = flashcardService.listByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @GetMapping("/decks/{deckId}/cards")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_VIEW')")
    @Operation(summary = "List flashcards in a deck")
    public ResponseEntity<ApiResponse<PagedResponse<FlashcardResponse>>> listByDeck(
            @PathVariable Long deckId, Pageable pageable) {
        var page = flashcardService.listByDeck(deckId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_EDIT')")
    @Operation(summary = "Update flashcard content")
    public ResponseEntity<ApiResponse<FlashcardResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody FlashcardUpdateRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Flashcard updated", flashcardService.update(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_DELETE')")
    @Operation(summary = "Soft delete flashcard")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        var userId = currentUserResolver.resolveCurrentUserId();
        flashcardService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Flashcard deleted", null));
    }

    @PostMapping("/batch-update")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_EDIT')")
    @Operation(summary = "Batch update flashcards")
    public ResponseEntity<ApiResponse<Void>> batchUpdate(
            @Valid @RequestBody FlashcardBatchUpdateRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        flashcardService.batchUpdate(request, userId);
        return ResponseEntity.ok(ApiResponse.success("Batch update completed", null));
    }

    @GetMapping("/export/{deckId}")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_EXPORT')")
    @Operation(summary = "Export deck as CSV or JSON")
    public ResponseEntity<ApiResponse<FlashcardExportResponse>> export(
            @PathVariable Long deckId,
            @RequestParam(defaultValue = "json") String format) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(flashcardService.exportDeck(deckId, format, userId)));
    }

    @GetMapping("/stats")
    @PreAuthorize("@permissionService.hasPermission('FLASHCARD_VIEW')")
    @Operation(summary = "Get flashcard review statistics")
    public ResponseEntity<ApiResponse<FlashcardStatsResponse>> stats() {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(flashcardService.getStats(userId)));
    }
}
