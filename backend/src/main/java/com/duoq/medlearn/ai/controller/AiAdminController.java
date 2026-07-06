package com.duoq.medlearn.ai.controller;

import com.duoq.medlearn.ai.dto.response.ModelInfo;
import com.duoq.medlearn.ai.dto.response.AiMessage;
import com.duoq.medlearn.ai.dto.response.PromptTemplateResponse;
import com.duoq.medlearn.ai.dto.response.PromptTestResultResponse;
import com.duoq.medlearn.ai.gateway.AiGatewayRouter;
import com.duoq.medlearn.ai.prompt.PromptBuilder;
import com.duoq.medlearn.ai.prompt.PromptTemplateService;
import com.duoq.medlearn.ai.prompt.PromptTester;
import com.duoq.medlearn.ai.prompt.PromptValidator;
import com.duoq.medlearn.ai.prompt.VariablesExtractor;
import com.duoq.medlearn.domain.dto.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai/admin")
@RequiredArgsConstructor
public class AiAdminController {

    private final PromptTemplateService promptTemplateService;
    private final PromptBuilder promptBuilder;
    private final PromptValidator promptValidator;
    private final VariablesExtractor variablesExtractor;
    private final PromptTester promptTester;
    private final AiGatewayRouter gatewayRouter;

    // ===== Prompt Template CRUD =====

    @GetMapping("/prompts")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<List<PromptTemplateResponse>>> listPrompts() {
        return ResponseEntity.ok(ApiResponse.success(promptTemplateService.listAll()));
    }

    @GetMapping("/prompts/{code}")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> getPrompt(
            @PathVariable String code,
            @RequestParam(required = false) String version) {
        if (version != null) {
            return ResponseEntity.ok(ApiResponse.success(promptTemplateService.findByCodeAndVersion(code, version)));
        }
        return ResponseEntity.ok(ApiResponse.success(promptTemplateService.findActiveByCode(code)));
    }

    @PostMapping("/prompts")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> createPrompt(
            @Valid @RequestBody PromptTemplateResponse dto) {
        promptValidator.validateOrThrow(dto.getSystemPrompt(), dto.getUserPromptTemplate());
        var created = promptTemplateService.create(dto, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(created));
    }

    @PutMapping("/prompts/{id}")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<PromptTemplateResponse>> updatePrompt(
            @PathVariable Long id, @RequestBody PromptTemplateResponse dto) {
        var updated = promptTemplateService.update(id, dto);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }

    @DeleteMapping("/prompts/{id}")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deletePrompt(@PathVariable Long id) {
        promptTemplateService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Prompt template deleted", null));
    }

    @GetMapping("/prompts/{code}/versions")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<List<PromptTemplateResponse>>> listVersions(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(promptTemplateService.listVersions(code)));
    }

    // ===== Prompt Validation =====

    @PostMapping("/prompts/validate")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<List<String>>> validatePrompt(@RequestBody Map<String, String> body) {
        var errors = promptValidator.validate(
                body.getOrDefault("systemPrompt", ""),
                body.getOrDefault("userPromptTemplate", ""));
        if (errors.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("Validation passed", List.of()));
        }
        return ResponseEntity.badRequest().body(ApiResponse.error("Validation failed: " + String.join("; ", errors)));
    }

    @PostMapping("/prompts/extract-variables")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> extractVariables(@RequestBody Map<String, String> body) {
        var template = body.getOrDefault("template", "");
        var variables = variablesExtractor.extract(template);
        return ResponseEntity.ok(ApiResponse.success(Map.of("variables", variables)));
    }

    // ===== Prompt Testing =====

    @PostMapping("/prompts/test")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<PromptTestResultResponse>> testPrompt(@RequestBody Map<String, Object> body) {
        var code = (String) body.get("code");
        var version = (String) body.get("version");
        @SuppressWarnings("unchecked")
        var variables = (Map<String, String>) body.get("variables");
        var expectedOutput = (String) body.get("expectedOutput");
        var model = (String) body.get("model");

        var result = promptTester.test(code, version, variables, expectedOutput, model, null);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    @GetMapping("/prompts/{code}/test-results")
    @PreAuthorize("hasAuthority('AI_MANAGE')")
    public ResponseEntity<ApiResponse<List<PromptTestResultResponse>>> getTestResults(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(promptTester.getResults(code)));
    }

    // ===== Model Discovery =====

    @GetMapping("/models")
    @PreAuthorize("hasAuthority('AI_VIEW_USAGE')")
    public ResponseEntity<ApiResponse<Map<String, List<ModelInfo>>>> listModels(
            @RequestParam(required = false) String provider) {
        if (provider != null) {
            var models = gatewayRouter.listModels(provider);
            return ResponseEntity.ok(ApiResponse.success(Map.of(provider, models)));
        }
        return ResponseEntity.ok(ApiResponse.success(gatewayRouter.listAllModels()));
    }

    // ===== Build Prompt =====

    @PostMapping("/prompts/build")
    @PreAuthorize("hasAuthority('AI_USE')")
    public ResponseEntity<ApiResponse<List<AiMessage>>> buildPrompt(@RequestBody Map<String, Object> body) {
        var code = (String) body.get("code");
        var version = (String) body.get("version");
        @SuppressWarnings("unchecked")
        var variables = (Map<String, String>) body.get("variables");

        var messages = version != null
                ? promptBuilder.build(code, version, variables)
                : promptBuilder.build(code, variables);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }
}
