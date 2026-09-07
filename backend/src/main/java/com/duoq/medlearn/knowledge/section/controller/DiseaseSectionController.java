package com.duoq.medlearn.knowledge.section.controller;

import com.duoq.medlearn.common.dto.ApiResponse;
import com.duoq.medlearn.knowledge.section.dto.request.*;
import com.duoq.medlearn.knowledge.section.dto.response.*;
import com.duoq.medlearn.knowledge.section.service.DiseaseSectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sections")
@RequiredArgsConstructor
@Tag(name = "Disease Sections", description = "Disease content section APIs")
public class DiseaseSectionController {

    private final DiseaseSectionService diseaseSectionService;

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Create section")
    public ResponseEntity<ApiResponse<DiseaseSectionResponse>> createSection(
            @RequestParam Long versionId,
            @Valid @RequestBody CreateDiseaseSectionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Section created",
                diseaseSectionService.createSection(versionId, request)
        ));
    }

    @PostMapping("/batch")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Create sections in batch")
    public ResponseEntity<ApiResponse<List<DiseaseSectionResponse>>> createSections(
            @RequestParam Long versionId,
            @Valid @RequestBody List<CreateDiseaseSectionRequest> requests
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Sections created",
                diseaseSectionService.createSections(versionId, requests)
        ));
    }

    @GetMapping("/{sectionId}")
    @Operation(summary = "Get section by ID")
    public ResponseEntity<ApiResponse<DiseaseSectionResponse>> getSectionById(@PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseSectionService.getSectionById(sectionId)));
    }

    @GetMapping("/version/{versionId}")
    @Operation(summary = "List sections by version")
    public ResponseEntity<ApiResponse<List<DiseaseSectionResponse>>> getSectionsByVersion(@PathVariable Long versionId) {
        return ResponseEntity.ok(ApiResponse.success(diseaseSectionService.getSectionsByVersion(versionId)));
    }

    @GetMapping("/version/{versionId}/type/{sectionType}")
    @Operation(summary = "Get section by version and type")
    public ResponseEntity<ApiResponse<DiseaseSectionResponse>> getSectionByType(
            @PathVariable Long versionId,
            @PathVariable String sectionType
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                diseaseSectionService.getSectionByType(versionId, sectionType)
        ));
    }

    @PutMapping("/{sectionId}")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Update section")
    public ResponseEntity<ApiResponse<DiseaseSectionResponse>> updateSection(
            @PathVariable Long sectionId,
            @Valid @RequestBody UpdateDiseaseSectionRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Section updated",
                diseaseSectionService.updateSection(sectionId, request)
        ));
    }

    @PostMapping("/version/{versionId}/reorder")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Reorder sections")
    public ResponseEntity<ApiResponse<Void>> reorderSections(
            @PathVariable Long versionId,
            @Valid @RequestBody List<SectionOrderRequest> requests
    ) {
        diseaseSectionService.reorderSections(versionId, requests);
        return ResponseEntity.ok(ApiResponse.success("Sections reordered", null));
    }

    @DeleteMapping("/{sectionId}")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Delete section")
    public ResponseEntity<ApiResponse<Void>> deleteSection(@PathVariable Long sectionId) {
        diseaseSectionService.deleteSection(sectionId);
        return ResponseEntity.ok(ApiResponse.success("Section deleted", null));
    }

    @DeleteMapping("/version/{versionId}")
    @PreAuthorize("@permissionService.hasPermission('VERSION_DELETE')")
    @Operation(summary = "Soft delete sections by version")
    public ResponseEntity<ApiResponse<Void>> softDeleteSectionsByVersion(@PathVariable Long versionId) {
        diseaseSectionService.softDeleteSectionsByVersion(versionId);
        return ResponseEntity.ok(ApiResponse.success("Sections soft deleted", null));
    }

    @PostMapping("/version/{versionId}/validate")
    @PreAuthorize("@permissionService.hasPermission('VERSION_WRITE')")
    @Operation(summary = "Validate required sections")
    public ResponseEntity<ApiResponse<Void>> validateRequiredSections(@PathVariable Long versionId) {
        diseaseSectionService.validateRequiredSections(versionId);
        return ResponseEntity.ok(ApiResponse.success("Sections validated", null));
    }

    @GetMapping("/{sectionId}/markdown")
    @Operation(summary = "Render section markdown")
    public ResponseEntity<ApiResponse<String>> renderMarkdownContent(@PathVariable Long sectionId) {
        return ResponseEntity.ok(ApiResponse.success(
                diseaseSectionService.renderMarkdownContent(sectionId)
        ));
    }

    @GetMapping("/types")
    @Operation(summary = "List section types")
    public ResponseEntity<ApiResponse<List<SectionTypeResponse>>> getAllSectionTypes() {
        return ResponseEntity.ok(ApiResponse.success(diseaseSectionService.getAllSectionTypes()));
    }

    @GetMapping("/templates")
    @Operation(summary = "List default section templates")
    public ResponseEntity<ApiResponse<List<SectionTemplateResponse>>> getDefaultTemplates() {
        return ResponseEntity.ok(ApiResponse.success(diseaseSectionService.getDefaultTemplates()));
    }
}
