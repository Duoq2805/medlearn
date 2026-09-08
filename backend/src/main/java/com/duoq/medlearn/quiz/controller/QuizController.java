package com.duoq.medlearn.quiz.controller;

import com.duoq.medlearn.common.dto.ApiResponse;
import com.duoq.medlearn.common.dto.PagedResponse;
import com.duoq.medlearn.common.security.CurrentUserResolver;
import com.duoq.medlearn.quiz.dto.request.QuizGenerateRequest;
import com.duoq.medlearn.quiz.dto.request.QuizSubmitRequest;
import com.duoq.medlearn.quiz.dto.response.*;
import com.duoq.medlearn.quiz.service.QuizService;
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
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quizzes", description = "AI-powered quiz generation and management")
public class QuizController {

    private final QuizService quizService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping("/generate")
    @PreAuthorize("@permissionService.hasPermission('QUIZ_GENERATE')")
    @Operation(summary = "Generate a quiz from a disease or document")
    public ResponseEntity<ApiResponse<QuizGenerateResponse>> generate(
            @Valid @RequestBody QuizGenerateRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        var response = quizService.generate(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Generated " + response.getTotalGenerated() + " questions", response));
    }

    @GetMapping("/{quizId}")
    @PreAuthorize("@permissionService.hasPermission('QUIZ_VIEW')")
    @Operation(summary = "Get quiz by ID with questions (no correct answers)")
    public ResponseEntity<ApiResponse<QuizResponse>> getById(@PathVariable Long quizId) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(quizService.getById(quizId, userId)));
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission('QUIZ_VIEW')")
    @Operation(summary = "List quizzes for current user")
    public ResponseEntity<ApiResponse<PagedResponse<QuizResponse>>> listByUser(Pageable pageable) {
        var userId = currentUserResolver.resolveCurrentUserId();
        var page = quizService.listByUser(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @PostMapping("/{quizId}/submit")
    @PreAuthorize("@permissionService.hasPermission('QUIZ_ATTEMPT')")
    @Operation(summary = "Submit quiz answers — returns graded results with correct answers")
    public ResponseEntity<ApiResponse<QuizSubmitResponse>> submit(
            @PathVariable Long quizId,
            @Valid @RequestBody QuizSubmitRequest request) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Quiz submitted", quizService.submit(quizId, request, userId)));
    }

    @GetMapping("/attempts")
    @PreAuthorize("@permissionService.hasPermission('QUIZ_VIEW')")
    @Operation(summary = "List past quiz attempts for current user")
    public ResponseEntity<ApiResponse<PagedResponse<QuizAttemptResponse>>> listAttempts(Pageable pageable) {
        var userId = currentUserResolver.resolveCurrentUserId();
        var page = quizService.listAttempts(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }
}
