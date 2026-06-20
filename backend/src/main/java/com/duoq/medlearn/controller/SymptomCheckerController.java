package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.ai.DiseaseMatchResultDTO;
import com.duoq.medlearn.domain.dto.ai.SymptomCheckerRequest;
import com.duoq.medlearn.domain.dto.ai.SymptomMatchResult;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.service.SymptomCheckerService;
import com.duoq.medlearn.service.SymptomCheckerServiceV1;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symptom-checker")
@RequiredArgsConstructor
public class SymptomCheckerController {

    private final SymptomCheckerService symptomCheckerService;
    private final SymptomCheckerServiceV1 symptomCheckerServiceV1;

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<List<SymptomMatchResult>>> checkSymptoms(
            @RequestBody List<Long> symptomIds,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom check completed",
                symptomCheckerService.checkSymptoms(symptomIds, limit)
        ));
    }

    @PostMapping("/analyze")
    public ResponseEntity<ApiResponse<List<DiseaseMatchResultDTO>>> analyzeSymptoms(
            @RequestBody SymptomCheckerRequest request
    ) {
        List<DiseaseMatchResultDTO> results = symptomCheckerServiceV1.analyze(request.getSymptomIds());
        return ResponseEntity.ok(ApiResponse.success(
                "Symptom analysis completed",
                results
        ));
    }
}
