package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.section.*;
import com.duoq.medlearn.service.DiseaseSectionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
public class DiseaseSectionController {

    private final DiseaseSectionService diseaseSectionService;

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseSectionDTO>> createSection(
            @RequestParam Long versionId,
            @Valid @RequestBody CreateDiseaseSectionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Section created",
                diseaseSectionService.createSection(versionId, request)
        ));
    }

    @PostMapping("/batch")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<List<DiseaseSectionDTO>>> createSections(
            @RequestParam Long versionId,
            @Valid @RequestBody List<CreateDiseaseSectionRequest> requests
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sections created",
                diseaseSectionService.createSections(versionId, requests)
        ));
    }

    @GetMapping("/{sectionId}")
    public ResponseEntity<ApiResponse<DiseaseSectionDTO>> getSectionById(@PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseSectionService.getSectionById(sectionId)));
    }

    @GetMapping("/version/{versionId}")
    public ResponseEntity<ApiResponse<List<DiseaseSectionDTO>>> getSectionsByVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseSectionService.getSectionsByVersion(versionId)));
    }

    @GetMapping("/version/{versionId}/type/{sectionType}")
    public ResponseEntity<ApiResponse<DiseaseSectionDTO>> getSectionByType(
            @PathVariable Long versionId,
            @PathVariable String sectionType
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                diseaseSectionService.getSectionByType(versionId, sectionType)
        ));
    }

    @PutMapping("/{sectionId}")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<DiseaseSectionDTO>> updateSection(
            @PathVariable Long sectionId,
            @Valid @RequestBody UpdateDiseaseSectionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Section updated",
                diseaseSectionService.updateSection(sectionId, request)
        ));
    }

    @PostMapping("/version/{versionId}/reorder")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<Void>> reorderSections(
            @PathVariable Long versionId,
            @Valid @RequestBody List<SectionOrderRequest> requests
    ) {
        diseaseSectionService.reorderSections(versionId, requests);
        return ResponseEntity.ok(ApiResponse.success("Sections reordered", null));
    }

    @DeleteMapping("/{sectionId}")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<Void>> deleteSection(@PathVariable Long sectionId) {
        diseaseSectionService.deleteSection(sectionId);
        return ResponseEntity.ok(ApiResponse.success("Section deleted", null));
    }

    @DeleteMapping("/version/{versionId}")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_DELETE)")
    public ResponseEntity<ApiResponse<Void>> softDeleteSectionsByVersion(@PathVariable Long versionId) {
        diseaseSectionService.softDeleteSectionsByVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success("Sections soft deleted", null));
    }

    @PostMapping("/version/{versionId}/validate")
    @PreAuthorize("@permissionService.hasPermission(T(com.duoq.medlearn.domain.enums.PermissionCode).VERSION_WRITE)")
    public ResponseEntity<ApiResponse<Void>> validateRequiredSections(@PathVariable Long versionId) {
        diseaseSectionService.validateRequiredSections(versionId);
        return ResponseEntity.ok(ApiResponse.success("Sections validated", null));
    }

    @GetMapping("/{sectionId}/markdown")
    public ResponseEntity<ApiResponse<String>> renderMarkdownContent(@PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success(
                diseaseSectionService.renderMarkdownContent(sectionId)
        ));
    }

    @GetMapping("/types")
    public ResponseEntity<ApiResponse<List<SectionTypeDTO>>> getAllSectionTypes() {
        return ResponseEntity.ok(ApiResponse.success(diseaseSectionService.getAllSectionTypes()));
    }

    @GetMapping("/templates")
    public ResponseEntity<ApiResponse<List<SectionTemplateDTO>>> getDefaultTemplates() {
        return ResponseEntity.ok(ApiResponse.success(diseaseSectionService.getDefaultTemplates()));
    }
}
