package com.duoq.medlearn.draft.controller;

import com.duoq.medlearn.ai.draft.AiDraftGenerator;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.draft.dto.AiDraftRequest;
import com.duoq.medlearn.draft.dto.DiseaseDraftResponse;
import com.duoq.medlearn.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai/drafts")
@RequiredArgsConstructor
@Tag(name = "AI Drafts", description = "AI-powered disease draft generation")
public class AiDraftController {

    private final AiDraftGenerator aiDraftGenerator;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('AI_DRAFT')")
    @Operation(summary = "Generate draft using AI",
            description = "Generate a disease draft using AI from optional document sources")
    public ResponseEntity<ApiResponse<DiseaseDraftResponse>> generate(@Valid @RequestBody AiDraftRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("AI draft generation started",
                aiDraftGenerator.generate(request, userId)));
    }
}
