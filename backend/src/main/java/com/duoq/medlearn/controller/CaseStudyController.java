package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.casestudy.CaseStudyDetailResponse;
import com.duoq.medlearn.domain.dto.casestudy.CaseStudySummaryProjection;
import com.duoq.medlearn.domain.dto.casestudy.CreateCaseStudyRequest;
import com.duoq.medlearn.domain.dto.casestudy.DiagnoseRequest;
import com.duoq.medlearn.domain.dto.casestudy.DiagnoseResponse;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.common.PagedResponse;
import com.duoq.medlearn.service.CaseStudyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
@Tag(name = "Case Studies", description = "Case study APIs")
public class CaseStudyController {

    private final CaseStudyService caseStudyService;

    @GetMapping
    @Operation(summary = "List approved case studies")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PagedResponse<CaseStudySummaryProjection>>> getAllCases(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(caseStudyService.getAllCases(pageable))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get case study by ID")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<CaseStudyDetailResponse>> getCaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(caseStudyService.getCaseById(id)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get case study by slug")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<CaseStudyDetailResponse>> getCaseBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(caseStudyService.getCaseBySlug(slug)));
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('DISEASE_WRITE')")
    @Operation(summary = "Create case study")
    public ResponseEntity<ApiResponse<CaseStudyDetailResponse>> createCase(@Valid @RequestBody CreateCaseStudyRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Case study created", caseStudyService.createCase(request)));
    }

    @PostMapping("/{id}/diagnose")
    @Operation(summary = "Submit case study diagnosis")
    public ResponseEntity<ApiResponse<DiagnoseResponse>> submitDiagnosis(@PathVariable Long id,
                                                                          @Valid @RequestBody DiagnoseRequest request) {
        return ResponseEntity.ok(ApiResponse.success(caseStudyService.submitDiagnosis(id, request)));
    }
}
