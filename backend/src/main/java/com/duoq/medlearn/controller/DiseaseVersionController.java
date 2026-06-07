package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.version.CreateDiseaseVersionRequest;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionDTO;
import com.duoq.medlearn.domain.dto.version.ModerationRequest;
import com.duoq.medlearn.domain.dto.version.UpdateDiseaseVersionRequest;
import com.duoq.medlearn.service.DiseaseVersionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
public class DiseaseVersionController {

    private final DiseaseVersionService diseaseVersionService;

    @PostMapping("/draft")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> createDraftVersion(
            @RequestParam Long diseaseId,
            @Valid @RequestBody CreateDiseaseVersionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Draft version created",
                diseaseVersionService.createDraftVersion(diseaseId, request)
        ));
    }

    @PostMapping("/clone/{diseaseId}")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> cloneApprovedVersion(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version cloned",
                diseaseVersionService.cloneApprovedVersion(diseaseId)
        ));
    }

    @GetMapping("/{versionId}")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> getVersionById(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseVersionService.getVersionById(versionId)));
    }

    @GetMapping("/disease/{diseaseId}")
    public ResponseEntity<ApiResponse<List<DiseaseVersionDTO>>> getDiseaseVersions(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseVersionService.getDiseaseVersions(diseaseId)));
    }

    @GetMapping("/disease/{diseaseId}/current")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> getCurrentApprovedVersion(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseVersionService.getCurrentApprovedVersion(diseaseId)));
    }

    @GetMapping("/disease/{diseaseId}/latest-draft")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_READ)")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> getLatestDraftVersion(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseVersionService.getLatestDraftVersion(diseaseId)));
    }

    @GetMapping("/pending-review")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_REVIEW)")
    public ResponseEntity<ApiResponse<Page<DiseaseVersionDTO>>> getPendingReviewVersions(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(diseaseVersionService.getPendingReviewVersions(pageable)));
    }

    @PutMapping("/{versionId}")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> updateDraftVersion(
            @PathVariable Long versionId,
            @Valid @RequestBody UpdateDiseaseVersionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Draft version updated",
                diseaseVersionService.updateDraftVersion(versionId, request)
        ));
    }

    @PostMapping("/{versionId}/submit")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> submitForReview(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version submitted for review",
                diseaseVersionService.submitForReview(versionId)
        ));
    }

    @PostMapping("/{versionId}/approve")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_REVIEW)")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> approveVersion(
            @PathVariable Long versionId,
            @Valid @RequestBody ModerationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version approved",
                diseaseVersionService.approveVersion(versionId, request)
        ));
    }

    @PostMapping("/{versionId}/reject")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_REVIEW)")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> rejectVersion(
            @PathVariable Long versionId,
            @Valid @RequestBody ModerationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version rejected",
                diseaseVersionService.rejectVersion(versionId, request)
        ));
    }

    @PostMapping("/{versionId}/archive")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseVersionDTO>> archiveVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version archived",
                diseaseVersionService.archiveVersion(versionId)
        ));
    }

    @DeleteMapping("/{versionId}")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_DELETE)")
    public ResponseEntity<ApiResponse<Void>> softDeleteVersion(@PathVariable Long versionId) {
        diseaseVersionService.softDeleteVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success("Version soft deleted", null));
    }

    @PatchMapping("/{versionId}/restore")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_RESTORE)")
    public ResponseEntity<ApiResponse<Void>> restoreVersion(@PathVariable Long versionId) {
        diseaseVersionService.restoreVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success("Version restored", null));
    }
}
