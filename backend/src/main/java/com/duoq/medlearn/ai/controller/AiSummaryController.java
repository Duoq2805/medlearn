package com.duoq.medlearn.ai.controller;

import com.duoq.medlearn.ai.dto.response.SummaryResponse;
import com.duoq.medlearn.ai.dto.request.SummaryRequest;
import com.duoq.medlearn.ai.summary.AiSummaryService;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai/summaries")
@RequiredArgsConstructor
@Tag(name = "AI Summary", description = "AI-powered disease summary generation and retrieval")
public class AiSummaryController {

    private final AiSummaryService aiSummaryService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/{diseaseId}")
    @PreAuthorize("hasAuthority('AI_SUMMARY')")
    @Operation(summary = "Generate an AI summary for a disease")
    public ResponseEntity<ApiResponse<SummaryResponse>> generate(
            @PathVariable Long diseaseId,
            @Valid @RequestBody SummaryRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        var summary = aiSummaryService.generate(diseaseId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Summary generated", summary));
    }

    @GetMapping("/{diseaseId}")
    @PreAuthorize("hasAuthority('AI_SUMMARY')")
    @Operation(summary = "List all summaries for a disease")
    public ResponseEntity<ApiResponse<List<SummaryResponse>>> listByDisease(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(ApiResponse.success(aiSummaryService.listByDisease(diseaseId)));
    }

    @GetMapping("/{diseaseId}/latest")
    @PreAuthorize("hasAuthority('AI_SUMMARY')")
    @Operation(summary = "Get the latest summary of a given type for a disease")
    public ResponseEntity<ApiResponse<SummaryResponse>> getLatest(
            @PathVariable Long diseaseId,
            @RequestParam(defaultValue = "STUDENT") String type) {
        return ResponseEntity.ok(ApiResponse.success(aiSummaryService.getLatest(diseaseId, type)));
    }

    @GetMapping("/{diseaseId}/versions/{version}")
    @PreAuthorize("hasAuthority('AI_SUMMARY')")
    @Operation(summary = "Get a specific version of a summary for a disease")
    public ResponseEntity<ApiResponse<SummaryResponse>> getByVersion(
            @PathVariable Long diseaseId,
            @PathVariable Integer version,
            @RequestParam(defaultValue = "STUDENT") String type) {
        return ResponseEntity.ok(ApiResponse.success(aiSummaryService.getByVersion(diseaseId, type, version)));
    }
}
