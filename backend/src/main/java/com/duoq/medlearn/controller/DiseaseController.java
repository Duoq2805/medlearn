package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.disease.DiseaseSummaryDTO;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseDraftRequest;
import com.duoq.medlearn.domain.dto.disease.CreateDiseaseRequest;
import com.duoq.medlearn.domain.dto.disease.DiseaseSearchRequest;
import com.duoq.medlearn.domain.dto.disease.UpdateDiseaseRequest;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.disease.DiseaseDTO;
import com.duoq.medlearn.domain.dto.disease.DiseaseDetailDTO;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionDTO;
import com.duoq.medlearn.service.DiseaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
public class DiseaseController {

    private final DiseaseService diseaseService;

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).DISEASE_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseDTO>> createDisease(@Valid @RequestBody CreateDiseaseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Disease created", diseaseService.createDisease(request)));
    }

    @PostMapping("/draft")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).DISEASE_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseDTO>> createDiseaseDraft(@Valid @RequestBody CreateDiseaseDraftRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Disease draft created", diseaseService.createDiseaseDraft(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DiseaseDTO>> getDiseaseById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(diseaseService.getDiseaseById(id)));
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<ApiResponse<DiseaseDetailDTO>> getDiseaseBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(ApiResponse.success(diseaseService.getDiseaseBySlug(slug)));
    }

    @GetMapping("/{id}/current-version")
    public ResponseEntity<ApiResponse<DiseaseDetailDTO>> getDiseaseCurrentVersion(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(diseaseService.getDiseaseCurrentVersion(id)));
    }

    @GetMapping("/approved")
    public ResponseEntity<ApiResponse<Page<DiseaseSummaryDTO>>> getApprovedDiseases(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) List<Long> symptomIds,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                diseaseService.getApprovedDiseases(keyword, categoryId, symptomIds, pageable)
        ));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<Page<DiseaseSummaryDTO>>> searchDiseases(
            @RequestBody(required = false) DiseaseSearchRequest request,
            Pageable pageable
    ) {
        return ResponseEntity.ok(ApiResponse.success(diseaseService.searchDiseases(request, pageable)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).DISEASE_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseDTO>> updateDiseaseMetadata(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDiseaseRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success("Disease updated", diseaseService.updateDiseaseMetadata(id, request)));
    }

    @PostMapping("/{id}/clone-current-version")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> cloneCurrentVersion(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Current version cloned", diseaseService.cloneCurrentVersion(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).DISEASE_DELETE)")
    public ResponseEntity<ApiResponse<Void>> softDeleteDisease(@PathVariable Long id) {
        diseaseService.softDeleteDisease(id);
        return ResponseEntity.ok(ApiResponse.success("Disease soft deleted", null));
    }

    @PatchMapping("/{id}/restore")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).DISEASE_RESTORE)")
    public ResponseEntity<ApiResponse<Void>> restoreDisease(@PathVariable Long id) {
        diseaseService.restoreDisease(id);
        return ResponseEntity.ok(ApiResponse.success("Disease restored", null));
    }

    @PatchMapping("/{id}/category/{categoryId}")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).DISEASE_MANAGE)")
    public ResponseEntity<ApiResponse<Void>> assignCategory(@PathVariable Long id, @PathVariable Long categoryId) {
        diseaseService.assignCategory(id, categoryId);
        return ResponseEntity.ok(ApiResponse.success("Category assigned", null));
    }

    @DeleteMapping("/{id}/category")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).DISEASE_MANAGE)")
    public ResponseEntity<ApiResponse<Void>> removeCategory(@PathVariable Long id) {
        diseaseService.removeCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category removed", null));
    }
}
