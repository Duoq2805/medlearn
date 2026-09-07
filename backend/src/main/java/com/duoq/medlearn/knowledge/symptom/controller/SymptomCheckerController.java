package com.duoq.medlearn.knowledge.symptom.controller;

import com.duoq.medlearn.knowledge.symptom.dto.request.SymptomCheckerRequest;
import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomAnalysisResponse;
import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomAnalysisV2Response;
import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomMatchResult;
import com.duoq.medlearn.knowledge.symptom.service.SymptomCheckerService;
import com.duoq.medlearn.knowledge.symptom.service.SymptomCheckerServiceV1;
import com.duoq.medlearn.knowledge.symptom.service.SymptomCheckerServiceV2;
import com.duoq.medlearn.common.dto.ApiResponse;
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
    private final SymptomCheckerServiceV2 symptomCheckerServiceV2;

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
    public ResponseEntity<ApiResponse<List<SymptomAnalysisResponse>>> analyzeSymptoms(
            @Valid @RequestBody SymptomCheckerRequest request
    ) {
        List<SymptomAnalysisResponse> results = symptomCheckerServiceV1.analyze(request.getSymptomIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom analysis completed",
                results
        ));
    }

    @PostMapping("/v2/analyze")
    @Operation(summary = "Analyze symptoms (V2)", description = "Deterministic clinical ranking with confidence, recommendations, and red flags")
    public ResponseEntity<ApiResponse<List<SymptomAnalysisV2Response>>> analyzeSymptomsV2(
            @Valid @RequestBody SymptomCheckerRequest request
    ) {
        List<SymptomAnalysisV2Response> results = symptomCheckerServiceV2.analyze(request.getSymptomIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom analysis V2 completed",
                results
        ));
    }
}
