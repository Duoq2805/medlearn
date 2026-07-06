package com.duoq.medlearn.document.controller;

import com.duoq.medlearn.document.dto.DocumentChunkResponse;
import com.duoq.medlearn.document.dto.DocumentResponse;
import com.duoq.medlearn.document.service.DocumentService;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.common.PagedResponse;
import com.duoq.medlearn.security.CurrentUserResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Documents", description = "Document upload and management")
public class DocumentController {

    private final DocumentService documentService;
    private final CurrentUserResolver currentUserResolver;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@permissionService.hasPermission('DOCUMENT_UPLOAD')")
    @Operation(summary = "Upload document")
    public ResponseEntity<ApiResponse<DocumentResponse>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String title) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Document uploaded", documentService.upload(file, title, userId)));
    }

    @PostMapping("/import-url")
    @PreAuthorize("@permissionService.hasPermission('DOCUMENT_UPLOAD')")
    @Operation(summary = "Import document from URL")
    public ResponseEntity<ApiResponse<DocumentResponse>> importFromUrl(
            @RequestParam @NotBlank String url,
            @RequestParam(required = false) String title) {
        var userId = currentUserResolver.resolveCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Document imported", documentService.importFromUrl(url, title, userId)));
    }

    @GetMapping
    @PreAuthorize("@permissionService.hasPermission('DOCUMENT_VIEW')")
    @Operation(summary = "List documents")
    public ResponseEntity<ApiResponse<PagedResponse<DocumentResponse>>> listDocuments(
            @RequestParam(defaultValue = "false") boolean admin, Pageable pageable) {
        var userId = currentUserResolver.resolveCurrentUserId();
        if (admin) {
            return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(documentService.listAll(pageable))));
        }
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(documentService.listByUser(userId, pageable))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('DOCUMENT_VIEW')")
    @Operation(summary = "Get document by ID")
    public ResponseEntity<ApiResponse<DocumentResponse>> getDocument(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getById(id)));
    }

    @GetMapping("/{id}/chunks")
    @PreAuthorize("@permissionService.hasPermission('DOCUMENT_VIEW')")
    @Operation(summary = "Get document chunks")
    public ResponseEntity<ApiResponse<List<DocumentChunkResponse>>> getChunks(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getChunks(id)));
    }

    @GetMapping("/{id}/download")
    @PreAuthorize("@permissionService.hasPermission('DOCUMENT_VIEW')")
    @Operation(summary = "Download document file")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        var doc = documentService.getById(id);
        var data = documentService.download(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + doc.getFileName() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('DOCUMENT_DELETE')")
    @Operation(summary = "Soft delete document")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        var userId = currentUserResolver.resolveCurrentUserId();
        documentService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Document deleted", null));
    }

    @DeleteMapping("/{id}/admin")
    @PreAuthorize("@permissionService.hasPermission('DOCUMENT_MANAGE')")
    @Operation(summary = "Admin: delete any document")
    public ResponseEntity<ApiResponse<Void>> adminDelete(@PathVariable Long id) {
        documentService.deleteAsAdmin(id);
        return ResponseEntity.ok(ApiResponse.success("Document deleted by admin", null));
    }
}
