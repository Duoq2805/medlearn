package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.common.PagedResponse;
import com.duoq.medlearn.domain.dto.version.CreateDiseaseVersionRequest;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionResponse;
import com.duoq.medlearn.domain.dto.version.ModerationRequest;
import com.duoq.medlearn.domain.dto.version.UpdateDiseaseVersionRequest;
import com.duoq.medlearn.service.DiseaseVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/versions")
@RequiredArgsConstructor
@Tag(name = "Disease Versions", description = "Disease version management and moderation workflow")
public class DiseaseVersionController {

    private final DiseaseVersionService diseaseVersionService;

    @PostMapping("/draft")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Create draft version", description = "Create a new draft version for a disease")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> createDraftVersion(
            @RequestParam Long diseaseId,
            @Valid @RequestBody CreateDiseaseVersionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Draft version created",
                diseaseVersionService.createDraftVersion(diseaseId, request)
        ));
    }

    @PostMapping("/clone/{diseaseId}")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Clone approved version", description = "Clone the current approved version into a new draft")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> cloneApprovedVersion(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version cloned",
                diseaseVersionService.cloneApprovedVersion(diseaseId)
        ));
    }

    @GetMapping("/{versionId}")
    @Operation(summary = "Get version by ID")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> getVersionById(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseVersionService.getVersionById(versionId)));
    }

    @GetMapping("/disease/{diseaseId}")
    @Operation(summary = "List all versions for a disease")
    public ResponseEntity<ApiResponse<List<DiseaseVersionResponse>>> getDiseaseVersions(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseVersionService.getDiseaseVersions(diseaseId)));
    }

    @GetMapping("/disease/{diseaseId}/current")
    @Operation(summary = "Get current approved version")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> getCurrentApprovedVersion(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseVersionService.getCurrentApprovedVersion(diseaseId)));
    }

    @GetMapping("/disease/{diseaseId}/latest-draft")
    @PreAuthorize("@permissionService.hasPermission('VERSION_READ')")
    @Operation(summary = "Get latest draft version for a disease")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> getLatestDraftVersion(@PathVariable Long diseaseId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseVersionService.getLatestDraftVersion(diseaseId)));
    }

    @GetMapping("/pending-review")
    @PreAuthorize("@permissionService.hasPermission('VERSION_REVIEW')")
    @Operation(summary = "List versions pending review")
    public ResponseEntity<ApiResponse<PagedResponse<DiseaseVersionResponse>>> getPendingReviewVersions(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(diseaseVersionService.getPendingReviewVersions(pageable))
        ));
    }

    @PutMapping("/{versionId}")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Update draft version content")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> updateDraftVersion(
            @PathVariable Long versionId,
            @Valid @RequestBody UpdateDiseaseVersionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Draft version updated",
                diseaseVersionService.updateDraftVersion(versionId, request)
        ));
    }

    @PostMapping("/{versionId}/submit")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Submit version for review")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> submitForReview(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version submitted for review",
                diseaseVersionService.submitForReview(versionId)
        ));
    }

    @PostMapping("/{versionId}/approve")
    @PreAuthorize("@permissionService.hasPermission('VERSION_REVIEW')")
    @Operation(summary = "Approve a version")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> approveVersion(
            @PathVariable Long versionId,
            @Valid @RequestBody ModerationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version approved",
                diseaseVersionService.approveVersion(versionId, request)
        ));
    }

    @PostMapping("/{versionId}/reject")
    @PreAuthorize("@permissionService.hasPermission('VERSION_REVIEW')")
    @Operation(summary = "Reject a version")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> rejectVersion(
            @PathVariable Long versionId,
            @Valid @RequestBody ModerationRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version rejected",
                diseaseVersionService.rejectVersion(versionId, request)
        ));
    }

    @PostMapping("/{versionId}/archive")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Archive a version")
    public ResponseEntity<ApiResponse<DiseaseVersionResponse>> archiveVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(
                "Version archived",
                diseaseVersionService.archiveVersion(versionId)
        ));
    }

    @DeleteMapping("/{versionId}")
    @PreAuthorize("@permissionService.hasPermission('VERSION_DELETE')")
    @Operation(summary = "Soft delete a version")
    public ResponseEntity<ApiResponse<Void>> softDeleteVersion(@PathVariable Long versionId) {
        diseaseVersionService.softDeleteVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success("Version soft deleted", null));
    }

    @PatchMapping("/{versionId}/restore")
    @PreAuthorize("@permissionService.hasPermission('VERSION_RESTORE')")
    @Operation(summary = "Restore soft-deleted version")
    public ResponseEntity<ApiResponse<Void>> restoreVersion(@PathVariable Long versionId) {
        diseaseVersionService.restoreVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success("Version restored", null));
    }
}
