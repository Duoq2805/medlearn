package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.common.PagedResponse;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseDraftRequest;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseResponse;
import com.duoq.medlearn.domain.dto.disease.DiseaseDetailResponse;
import com.duoq.medlearn.domain.dto.disease.DiseaseSearchRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseSummaryProjection;
import com.duoq.medlearn.domain.dto.disease.UpdateDiseaseRequest;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionResponse;
import com.duoq.medlearn.service.DiseaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
@Tag(name = "Diseases", description = "Disease content management")
public class DiseaseController {

    private final DiseaseService diseaseService;

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('DISEASE_WRITE')")
    @Operation(summary = "Create disease", description = "Create a new disease with an initial draft version")
    public ResponseEntity<ApiResponse<DiseaseResponse>> createDisease(@Valid @RequestBody CreateDiseaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Disease created", diseaseService.createDisease(request)));
    }

    @PostMapping("/draft")
    @PreAuthorize("@permissionService.hasPermission('DISEASE_WRITE')")
    @Operation(summary = "Create disease draft", description = "Create a new disease in draft state")
    public ResponseEntity<ApiResponse<DiseaseResponse>> createDiseaseDraft(@Valid @RequestBody CreateDiseaseDraftRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Disease draft created", diseaseService.createDiseaseDraft(request)));
    }

    @GetMapping
    @Operation(summary = "List approved diseases", description = "Browse approved diseases with optional keyword/category/symptom filters. Pagination: use ?page=0&size=20 (not pageSize)")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<PagedResponse<DiseaseSummaryProjection>>> getDiseases(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> symptomIds,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(diseaseService.getApprovedDiseases(keyword, categoryId, symptomIds, pageable))
        ));
    }

    @GetMapping("/search")
    @Operation(summary = "Search diseases", description = "Full-text search approved diseases by keyword")
    public ResponseEntity<ApiResponse<PagedResponse<DiseaseSummaryProjection>>> searchDiseases(
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(diseaseService.getApprovedDiseases(keyword, null, null, pageable))
        ));
    }

    @PostMapping("/search")
    @Operation(summary = "Advanced disease search", description = "Search diseases with advanced filters in request body")
    public ResponseEntity<ApiResponse<PagedResponse<DiseaseSummaryProjection>>> advancedSearch(
            @RequestBody(required = false) DiseaseSearchRequest request,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(diseaseService.searchDiseases(request, pageable))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get disease by ID")
    public ResponseEntity<ApiResponse<DiseaseResponse>> getDiseaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(diseaseService.getDiseaseById(id)));
    }

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Get disease by slug")
    public ResponseEntity<ApiResponse<DiseaseDetailResponse>> getDiseaseBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(diseaseService.getDiseaseBySlug(slug)));
    }

    @GetMapping("/{id}/current-version")
    @Operation(summary = "Get disease current approved version detail")
    public ResponseEntity<ApiResponse<DiseaseDetailResponse>> getDiseaseCurrentVersion(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(diseaseService.getDiseaseCurrentVersion(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('DISEASE_WRITE')")
    @Operation(summary = "Update disease metadata")
    public ResponseEntity<ApiResponse<DiseaseResponse>> updateDiseaseMetadata(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiseaseRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Disease updated", diseaseService.updateDiseaseMetadata(id, request)));
    }

    @PostMapping("/{id}/clone-current-version")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Clone current approved version into a new draft")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> cloneCurrentVersion(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Current version cloned", diseaseService.cloneCurrentVersion(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('DISEASE_DELETE')")
    @Operation(summary = "Soft delete disease")
    public ResponseEntity<ApiResponse<Void>> softDeleteDisease(@PathVariable Long id) {
        diseaseService.softDeleteDisease(id);
        return ResponseEntity.ok(ApiResponse.success("Disease soft deleted", null));
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("@permissionService.hasPermission('DISEASE_RESTORE')")
    @Operation(summary = "Restore soft-deleted disease")
    public ResponseEntity<ApiResponse<Void>> restoreDisease(@PathVariable Long id) {
        diseaseService.restoreDisease(id);
        return ResponseEntity.ok(ApiResponse.success("Disease restored", null));
    }

    @PatchMapping("/{id}/category/{categoryId}")
    @PreAuthorize("@permissionService.hasPermission('DISEASE_MANAGE')")
    @Operation(summary = "Assign category to disease")
    public ResponseEntity<ApiResponse<Void>> assignCategory(@PathVariable Long id, @PathVariable Long categoryId) {
        diseaseService.assignCategory(id, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category assigned", null));
    }

    @DeleteMapping("/{id}/category")
    @PreAuthorize("@permissionService.hasPermission('DISEASE_MANAGE')")
    @Operation(summary = "Remove category from disease")
    public ResponseEntity<ApiResponse<Void>> removeCategory(@PathVariable Long id) {
        diseaseService.removeCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category removed", null));
    }
}
