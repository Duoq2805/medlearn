package com.duoq.medlearn.ai.summary.impl;

import com.duoq.medlearn.ai.summary.AiSummaryService;
import com.duoq.medlearn.ai.dto.request.AiChatRequest;
import com.duoq.medlearn.ai.dto.response.SummaryResponse;
import com.duoq.medlearn.ai.dto.request.SummaryRequest;
import com.duoq.medlearn.ai.enums.SummaryType;
import com.duoq.medlearn.ai.exception.AiConfigurationException;
import com.duoq.medlearn.ai.gateway.AiGatewayRouter;
import com.duoq.medlearn.ai.model.AiModel;
import com.duoq.medlearn.ai.prompt.PromptBuilder;
import com.duoq.medlearn.ai.prompt.PromptTemplateService;
import com.duoq.medlearn.ai.repository.AiSummaryRepository;
import com.duoq.medlearn.ai.security.AiOutputValidator;
import com.duoq.medlearn.ai.summary.entity.AiSummary;
import com.duoq.medlearn.ai.usage.AiUsageService;
import com.duoq.medlearn.knowledge.disease.entity.Disease;
import com.duoq.medlearn.knowledge.disease.entity.DiseaseSection;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import com.duoq.medlearn.knowledge.disease.repository.DiseaseRepository;
import com.duoq.medlearn.knowledge.section.repository.DiseaseSectionRepository;
import com.duoq.medlearn.knowledge.version.repository.DiseaseVersionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiSummaryServiceImpl implements AiSummaryService {

    private static final String PROMPT_CODE = "disease-summary";
    private static final String REQUEST_TYPE = "SUMMARY";

    private final AiSummaryRepository aiSummaryRepository;
    private final DiseaseRepository diseaseRepository;
    private final DiseaseVersionRepository diseaseVersionRepository;
    private final DiseaseSectionRepository diseaseSectionRepository;
    private final PromptTemplateService promptTemplateService;
    private final PromptBuilder promptBuilder;
    private final AiGatewayRouter gatewayRouter;
    private final AiOutputValidator aiOutputValidator;
    private final AiUsageService aiUsageService;

    @Override
    @Transactional
    public SummaryResponse generate(Long diseaseId, SummaryRequest request, Long userId) {
        var disease = diseaseRepository.findById(diseaseId)
                .orElseThrow(() -> new ResourceNotFoundException("Disease not found: " + diseaseId));

        var version = findApprovedVersion(disease);
        var sections = diseaseSectionRepository.findAllByVersionIdWithType(version.getId());

        var variables = buildVariables(disease, sections, request.getSummaryType());

        var template = promptTemplateService.getActiveEntity(PROMPT_CODE);
        var messages = promptBuilder.buildFromTemplate(template, variables);

        var chatRequest = AiChatRequest.builder()
                .messages(messages)
                .model(request.getModel() != null ? request.getModel() : template.getModel())
                .temperature(request.getTemperature() != null ? request.getTemperature() : template.getTemperature())
                .maxTokens(template.getMaxTokens())
                .promptTemplateCode(PROMPT_CODE)
                .userId(String.valueOf(userId))
                .build();

        var startTime = System.currentTimeMillis();
        var response = gatewayRouter.chat(chatRequest, AiModel.fromModelId(chatRequest.getModel()));
        var latencyMs = System.currentTimeMillis() - startTime;

        var content = response.getChoices() != null && !response.getChoices().isEmpty()
                ? response.getChoices().getFirst().getContent()
                : "";

        content = aiOutputValidator.validate(content);

        var nextVersion = aiSummaryRepository.findMaxVersionByDiseaseIdAndSummaryType(diseaseId, request.getSummaryType()) + 1;

        var usage = response.getUsage();
        var entity = AiSummary.builder()
                .disease(disease)
                .diseaseVersion(version)
                .summaryType(request.getSummaryType())
                .version(nextVersion)
                .content(content)
                .model(response.getModel())
                .provider(response.getProvider())
                .promptTokens(usage != null ? usage.getPromptTokens() : 0)
                .completionTokens(usage != null ? usage.getCompletionTokens() : 0)
                .totalTokens(usage != null ? usage.getTotalTokens() : 0)
                .latencyMs((int) latencyMs)
                .createdBy(User.builder().id(userId).build())
                .build();

        var saved = aiSummaryRepository.save(entity);

        aiUsageService.log(
                User.builder().id(userId).build(),
                REQUEST_TYPE,
                response.getModel(),
                response.getProvider(),
                usage != null ? usage.getPromptTokens() : 0,
                usage != null ? usage.getCompletionTokens() : 0,
                usage != null ? usage.getTotalTokens() : 0,
                latencyMs,
                true,
                null,
                response.isCached()
        );

        log.info("AI summary generated: diseaseId={}, type={}, version={}, tokens={}",
                diseaseId, request.getSummaryType(), nextVersion, saved.getTotalTokens());

        return toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SummaryResponse> listByDisease(Long diseaseId) {
        return aiSummaryRepository.findAllByDiseaseIdAndDeletedAtIsNullOrderByCreatedAtDesc(diseaseId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SummaryResponse getLatest(Long diseaseId, String summaryType) {
        var type = parseSummaryType(summaryType);
        var entity = aiSummaryRepository
                .findTopByDiseaseIdAndSummaryTypeAndDeletedAtIsNullOrderByCreatedAtDesc(diseaseId, type)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No summary found for disease " + diseaseId + " type " + summaryType));
        return toDto(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public SummaryResponse getByVersion(Long diseaseId, String summaryType, Integer version) {
        var type = parseSummaryType(summaryType);
        var entity = aiSummaryRepository
                .findByDiseaseIdAndSummaryTypeAndVersionAndDeletedAtIsNull(diseaseId, type, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Summary not found for disease " + diseaseId + " type " + summaryType + " v" + version));
        return toDto(entity);
    }

    private DiseaseVersion findApprovedVersion(Disease disease) {
        if (!disease.hasCurrentVersion()) {
            throw new AiConfigurationException("Disease " + disease.getId() + " has no current version");
        }
        var version = diseaseVersionRepository.findById(disease.getCurrentVersion().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Current version not found for disease " + disease.getId()));
        if (!version.isApproved()) {
            throw new AiConfigurationException("Disease " + disease.getId() + " current version is not approved");
        }
        return version;
    }

    private Map<String, String> buildVariables(Disease disease, List<DiseaseSection> sections, SummaryType summaryType) {
        var variables = new HashMap<String, String>();
        variables.put("disease_name", disease.getName());
        variables.put("summary_type", summaryType.name());

        for (var section : sections) {
            if (section.getDeletedAt() == null && section.getSectionType() != null) {
                var key = section.getSectionType().getName().toLowerCase();
                variables.put(key, section.getContent() != null ? section.getContent() : "");
            }
        }
        return variables;
    }

    private SummaryType parseSummaryType(String value) {
        try {
            return SummaryType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AiConfigurationException("Invalid summary type: " + value
                    + ". Allowed: STUDENT, CLINICAL, EXAM, QUICK_REVISION");
        }
    }

    private SummaryResponse toDto(AiSummary entity) {
        return SummaryResponse.builder()
                .id(entity.getId())
                .diseaseId(entity.getDisease().getId())
                .diseaseName(entity.getDisease().getName())
                .diseaseVersionId(entity.getDiseaseVersion().getId())
                .summaryType(entity.getSummaryType())
                .version(entity.getVersion())
                .content(entity.getContent())
                .model(entity.getModel())
                .provider(entity.getProvider())
                .promptTokens(entity.getPromptTokens())
                .completionTokens(entity.getCompletionTokens())
                .totalTokens(entity.getTotalTokens())
                .latencyMs(entity.getLatencyMs())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
