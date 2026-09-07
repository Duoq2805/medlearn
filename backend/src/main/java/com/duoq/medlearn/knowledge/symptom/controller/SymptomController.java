package com.duoq.medlearn.knowledge.symptom.controller;

import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomMatchResult;
import com.duoq.medlearn.common.dto.ApiResponse;
import com.duoq.medlearn.knowledge.symptom.dto.request.CreateSymptomRequest;
import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomResponse;
import com.duoq.medlearn.knowledge.symptom.dto.request.UpdateSymptomRequest;
import com.duoq.medlearn.knowledge.symptom.service.SymptomCheckerService;
import com.duoq.medlearn.knowledge.symptom.service.SymptomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/symptoms")
@RequiredArgsConstructor
@Validated
@Tag(name = "Symptoms", description = "Symptom management API")
public class SymptomController {

    private final SymptomService symptomService;
    private final SymptomCheckerService symptomCheckerService;

    @GetMapping
    @Operation(summary = "Get all symptoms", description = "Retrieve a list of all symptoms.")
    public ResponseEntity<ApiResponse<List<SymptomResponse>>> getAllSymptoms() {
        return ResponseEntity.ok(ApiResponse.success(
                "Symptoms retrieved successfully",
                symptomService.getAllSymptoms()
        ));
    }

    @GetMapping("/search")
    @Operation(summary = "Search symptoms", description = "Search symptoms by keyword.")
    public ResponseEntity<ApiResponse<List<SymptomResponse>>> searchSymptoms(
            @RequestParam @NotBlank @Size(min = 2, max = 50) String keyword
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Symptoms searched successfully",
                symptomService.searchSymptoms(keyword)
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get symptom by ID", description = "Retrieve a single symptom by its unique identifier.")
    public ResponseEntity<ApiResponse<SymptomResponse>> getSymptomById(@PathVariable @NotNull @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom retrieved successfully",
                symptomService.getSymptomById(id)
        ));
    }

    @PostMapping
    @PreAuthorize("@permissionService.hasPermission('SYMPTOM_WRITE')")
    @Operation(summary = "Create a symptom", description = "Add a new symptom to the system.")
    public ResponseEntity<ApiResponse<SymptomResponse>> createSymptom(@Valid @RequestBody CreateSymptomRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom created successfully",
                symptomService.createSymptom(request)
        ));
    }

    @PutMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('SYMPTOM_WRITE')")
    @Operation(summary = "Update a symptom", description = "Modify an existing symptom by its ID.")
    public ResponseEntity<ApiResponse<SymptomResponse>> updateSymptom(
            @PathVariable @NotNull @Positive Long id,
            @RequestBody UpdateSymptomRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom updated successfully",
                symptomService.updateSymptom(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@permissionService.hasPermission('SYMPTOM_DELETE')")
    @Operation(summary = "Delete a symptom", description = "Remove a symptom by its ID.")
    public ResponseEntity<ApiResponse<Void>> deleteSymptom(@PathVariable @NotNull @Positive Long id) {
        symptomService.deleteSymptom(id);
        return ResponseEntity.ok(ApiResponse.success("Symptom deleted successfully", null));
    }

    // ---- Symptom Checker Endpoints ----

    // ---- Symptom Checker Endpoints ----

    @PostMapping("/check")
    @Operation(summary = "Check symptoms (V1)", description = "Match symptoms against diseases using basic scoring")
    public ResponseEntity<ApiResponse<List<SymptomMatchResult>>> checkSymptomsV1(
            @Valid @RequestBody SymptomCheckerRequest request
    ) {
        int limit = Optional.ofNullable(request.getLimit()).orElse(10);
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom check completed",
                symptomCheckerService.checkSymptoms(request.getSymptomIds(), limit)
        ));
    }

    // ---- DTOs for SymptomController ----

    @Data
    public static class SymptomCheckerRequest {
        @NotEmpty private List<Long> symptomIds;
        private Integer limit;
    }
}
