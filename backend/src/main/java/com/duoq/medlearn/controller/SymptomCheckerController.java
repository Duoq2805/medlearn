package com.duoq.medlearn.controller;

import com.duoq.medlearn.domain.dto.ai.SymptomMatchResult;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import com.duoq.medlearn.service.SymptomCheckerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/symptom-checker")
@RequiredArgsConstructor
public class SymptomCheckerController {

    private final SymptomCheckerService symptomCheckerService;

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
}
