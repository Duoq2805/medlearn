package com.duoq.medlearn.draft.controller;

import com.duoq.medlearn.draft.dto.request.*;
import com.duoq.medlearn.draft.dto.response.*;
import com.duoq.medlearn.draft.service.DraftLifecycleService;
import com.duoq.medlearn.draft.service.DraftService;
import com.duoq.medlearn.common.dto.ApiResponse;
import com.duoq.medlearn.common.dto.PagedResponse;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/drafts")
@RequiredArgsConstructor
@Tag(name = "Drafts", description = "Disease draft management (manual and AI generated content)")
public class DraftController {

    private final DraftService draftService;
    private final DraftLifecycleService draftLifecycleService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('DRAFT_CREATE')")
    @Operation(summary = "Create draft manually")
    public ResponseEntity<ApiResponse<DiseaseDraftResponse>> create(@Valid @RequestBody CreateDraftRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Draft created", draftService.create(request, userId)));
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission('DRAFT_VIEW')")
    @Operation(summary = "List drafts")
    public ResponseEntity<ApiResponse<PagedResponse<DiseaseDraftResponse>>> listDrafts(
            @RequestParam(required = false) Long diseaseId, Pageable pageable) {
        var userId = currentUserResolver.resolveCurrentUserId();
        if (diseaseId != null) {
            return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(draftService.listByDisease(diseaseId, pageable))));
        }
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(draftService.listByUser(userId, pageable))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('DRAFT_VIEW')")
    @Operation(summary = "Get draft by ID")
    public ResponseEntity<ApiResponse<DiseaseDraftResponse>> getDraft(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(draftService.getById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('DRAFT_CREATE')")
    @Operation(summary = "Update draft content")
    public ResponseEntity<ApiResponse<DiseaseDraftResponse>> update(
            @PathVariable Long id, @Valid @RequestBody UpdateDraftRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Draft updated", draftService.update(id, request, userId)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('DRAFT_DELETE')")
    @Operation(summary = "Soft delete draft")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        var userId = currentUserResolver.resolveCurrentUserId();
        draftService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Draft deleted", null));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("@permissionService.hasPermission('DRAFT_CREATE')")
    @Operation(summary = "Submit draft for review")
    public ResponseEntity<ApiResponse<DiseaseDraftResponse>> submit(@PathVariable Long id) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Draft submitted for review",
                draftLifecycleService.submitForReview(id, userId)));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("@permissionService.hasPermission('DRAFT_REVIEW')")
    @Operation(summary = "Approve draft")
    public ResponseEntity<ApiResponse<DiseaseDraftResponse>> approve(
            @PathVariable Long id, @Valid @RequestBody DraftReviewRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Draft approved",
                draftLifecycleService.approve(id, userId, request.getNote())));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("@permissionService.hasPermission('DRAFT_REVIEW')")
    @Operation(summary = "Reject draft")
    public ResponseEntity<ApiResponse<DiseaseDraftResponse>> reject(
            @PathVariable Long id, @Valid @RequestBody DraftReviewRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Draft rejected",
                draftLifecycleService.reject(id, userId, request.getNote())));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("@permissionService.hasPermission('DRAFT_DELETE')")
    @Operation(summary = "Archive draft")
    public ResponseEntity<ApiResponse<DiseaseDraftResponse>> archive(@PathVariable Long id) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Draft archived",
                draftLifecycleService.archive(id, userId)));
    }

    @PostMapping("/{id}/clone")
    @PreAuthorize("@permissionService.hasPermission('DRAFT_CREATE')")
    @Operation(summary = "Clone draft")
    public ResponseEntity<ApiResponse<DiseaseDraftResponse>> clone(@PathVariable Long id) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Draft cloned",
                draftLifecycleService.cloneDraft(id, userId)));
    }

    @PostMapping("/{id}/apply")
    @PreAuthorize("@permissionService.hasPermission('DRAFT_REVIEW')")
    @Operation(summary = "Apply approved draft to disease version")
    public ResponseEntity<ApiResponse<Void>> apply(@PathVariable Long id) {
        var userId = currentUserResolver.resolveCurrentUserId();
        draftLifecycleService.applyToDisease(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Draft applied to disease", null));
    }
}
