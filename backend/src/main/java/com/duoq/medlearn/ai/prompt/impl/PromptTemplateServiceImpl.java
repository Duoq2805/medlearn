package com.duoq.medlearn.ai.prompt.impl;

import com.duoq.medlearn.ai.prompt.PromptTemplateService;
import com.duoq.medlearn.ai.dto.response.PromptTemplateResponse;
import com.duoq.medlearn.ai.exception.AiConfigurationException;
import com.duoq.medlearn.ai.prompt.entity.PromptTemplate;
import com.duoq.medlearn.ai.repository.PromptTemplateRepository;
import com.duoq.medlearn.auth.entity.User;
import com.duoq.medlearn.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptTemplateServiceImpl implements PromptTemplateService {

    private final PromptTemplateRepository promptTemplateRepository;

    @Override
    @Transactional
    public PromptTemplateResponse create(PromptTemplateResponse dto, Long createdBy) {
        if (promptTemplateRepository.existsByCodeAndVersion(dto.getCode(), dto.getVersion())) {
            throw new AiConfigurationException(
                    "Prompt template already exists: " + dto.getCode() + " v" + dto.getVersion());
        }

        var entity = PromptTemplate.builder()
                .code(dto.getCode())
                .name(dto.getName())
                .description(dto.getDescription())
                .systemPrompt(dto.getSystemPrompt())
                .userPromptTemplate(dto.getUserPromptTemplate())
                .version(dto.getVersion() != null ? dto.getVersion() : "1.0")
                .model(dto.getModel())
                .temperature(dto.getTemperature())
                .maxTokens(dto.getMaxTokens())
                .requiredVariables(dto.getRequiredVariables() != null
                        ? String.join(",", dto.getRequiredVariables()) : null)
                .status(dto.isActive() ? "ACTIVE" : "INACTIVE")
                .createdBy(createdBy != null ? User.builder().id(createdBy).build() : null)
                .build();

        var saved = promptTemplateRepository.save(entity);
        return toDto(saved);
    }

    @Override
    @Transactional
    public PromptTemplateResponse update(Long id, PromptTemplateResponse dto) {
        var entity = promptTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt template not found: " + id));

        if (dto.getName() != null) entity.setName(dto.getName());
        if (dto.getDescription() != null) entity.setDescription(dto.getDescription());
        if (dto.getSystemPrompt() != null) entity.setSystemPrompt(dto.getSystemPrompt());
        if (dto.getUserPromptTemplate() != null) entity.setUserPromptTemplate(dto.getUserPromptTemplate());
        if (dto.getModel() != null) entity.setModel(dto.getModel());
        if (dto.getTemperature() != null) entity.setTemperature(dto.getTemperature());
        if (dto.getMaxTokens() != null) entity.setMaxTokens(dto.getMaxTokens());
        if (dto.getRequiredVariables() != null)
            entity.setRequiredVariables(String.join(",", dto.getRequiredVariables()));
        entity.setStatus(dto.isActive() ? "ACTIVE" : "INACTIVE");

        var saved = promptTemplateRepository.save(entity);
        return toDto(saved);
    }

    @Override
    public PromptTemplateResponse findByCodeAndVersion(String code, String version) {
        var entity = promptTemplateRepository.findByCodeAndVersion(code, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prompt template not found: " + code + " v" + version));
        return toDto(entity);
    }

    @Override
    public PromptTemplateResponse findActiveByCode(String code) {
        var entity = promptTemplateRepository
                .findTopByCodeAndStatusOrderByCreatedAtDesc(code, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active prompt template found for code: " + code));
        return toDto(entity);
    }

    @Override
    public List<PromptTemplateResponse> listVersions(String code) {
        return promptTemplateRepository.findByCodeOrderByCreatedAtDesc(code)
                .stream().map(this::toDto).toList();
    }

    @Override
    public List<PromptTemplateResponse> listAll() {
        return promptTemplateRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        var entity = promptTemplateRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prompt template not found: " + id));
        promptTemplateRepository.delete(entity);
    }

    @Override
    public PromptTemplate getActiveEntity(String code) {
        return promptTemplateRepository
                .findTopByCodeAndStatusOrderByCreatedAtDesc(code, "ACTIVE")
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active prompt template found for code: " + code));
    }

    @Override
    public PromptTemplate getEntity(String code, String version) {
        return promptTemplateRepository.findByCodeAndVersion(code, version)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Prompt template not found: " + code + " v" + version));
    }

    private PromptTemplateResponse toDto(PromptTemplate entity) {
        return PromptTemplateResponse.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .systemPrompt(entity.getSystemPrompt())
                .userPromptTemplate(entity.getUserPromptTemplate())
                .version(entity.getVersion())
                .model(entity.getModel())
                .temperature(entity.getTemperature())
                .maxTokens(entity.getMaxTokens())
                .requiredVariables(entity.getRequiredVariables() != null
                        ? List.of(entity.getRequiredVariables().split(",")) : List.of())
                .active("ACTIVE".equals(entity.getStatus()))
                .status(entity.getStatus())
                .build();
    }
}
