package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.domain.dto.symptom.CreateSymptomRequest;
import com.duoq.medlearn.domain.dto.symptom.SymptomDTO;
import com.duoq.medlearn.domain.dto.symptom.UpdateSymptomRequest;
import com.duoq.medlearn.service.SymptomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
@Tag(name = "Symptoms", description = "Symptom APIs")
public class SymptomController {

    private final SymptomService symptomService;

    @GetMapping
    @Operation(summary = "List symptoms")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<SymptomDTO>>> getAllSymptoms() {
        return ResponseEntity.ok(ApiResponse.success(symptomService.getAllSymptoms()));
    }

    @GetMapping("/search")
    @Operation(summary = "Search symptoms", description = "Search symptoms by keyword. Query parameter q is still accepted for backward compatibility.")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<SymptomDTO>>> searchSymptoms(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String q
    ) {
        String query = keyword != null ? keyword : q;
        return ResponseEntity.ok(ApiResponse.success(symptomService.searchSymptoms(query)));
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('DISEASE_WRITE')")
    @Operation(summary = "Create symptom")
    public ResponseEntity<ApiResponse<SymptomDTO>> createSymptom(@Valid @RequestBody CreateSymptomRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Symptom created", symptomService.createSymptom(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('DISEASE_WRITE')")
    @Operation(summary = "Update symptom")
    public ResponseEntity<ApiResponse<SymptomDTO>> updateSymptom(@PathVariable Long id,
                                                                  @Valid @RequestBody UpdateSymptomRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Symptom updated", symptomService.updateSymptom(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('DISEASE_DELETE')")
    @Operation(summary = "Delete symptom")
    public ResponseEntity<ApiResponse<Void>> deleteSymptom(@PathVariable Long id) {
        symptomService.deleteSymptom(id);
        return ResponseEntity.ok(ApiResponse.success("Symptom deleted", null));
    }
}
