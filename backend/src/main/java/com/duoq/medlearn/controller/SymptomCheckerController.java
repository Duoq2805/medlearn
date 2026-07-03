package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.ai.DiseaseMatchResultDTO;
import com.duoq.medlearn.domain.dto.ai.SymptomCheckerRequest;
import com.duoq.medlearn.domain.dto.ai.SymptomMatchResult;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.service.SymptomCheckerService;
import com.duoq.medlearn.service.SymptomCheckerServiceV1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symptom-checker")
@RequiredArgsConstructor
@Tag(name = "Symptom Checker", description = "Symptom-based disease matching")
public class SymptomCheckerController {

    private final SymptomCheckerService symptomCheckerService;
    private final SymptomCheckerServiceV1 symptomCheckerServiceV1;

    @PostMapping("/check")
    @Operation(summary = "Check symptoms (V0)", description = "Match symptoms against diseases using basic scoring")
    public ResponseEntity<ApiResponse<List<SymptomMatchResult>>> checkSymptoms(
            @Valid @RequestBody SymptomCheckerRequest request
    ) {
        int limit = request.getLimit() != null ? request.getLimit() : 10;
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom check completed",
                symptomCheckerService.checkSymptoms(request.getSymptomIds(), limit)
        ));
    }

    @PostMapping("/analyze")
    @Operation(summary = "Analyze symptoms (V1)", description = "Advanced symptom analysis with weighted scoring")
    public ResponseEntity<ApiResponse<List<DiseaseMatchResultDTO>>> analyzeSymptoms(
            @Valid @RequestBody SymptomCheckerRequest request
    ) {
        List<DiseaseMatchResultDTO> results = symptomCheckerServiceV1.analyze(request.getSymptomIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom analysis completed",
                results
        ));
    }
}
