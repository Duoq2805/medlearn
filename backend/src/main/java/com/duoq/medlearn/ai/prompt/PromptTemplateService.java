package com.duoq.medlearn.ai.prompt;

import com.duoq.medlearn.ai.dto.response.PromptTemplateResponse;
import com.duoq.medlearn.ai.prompt.entity.PromptTemplate;

import java.util.List;

public interface PromptTemplateService {

    PromptTemplateResponse create(PromptTemplateResponse dto, Long createdBy);

    PromptTemplateResponse update(Long id, PromptTemplateResponse dto);

    PromptTemplateResponse findByCodeAndVersion(String code, String version);

    PromptTemplateResponse findActiveByCode(String code);

    List<PromptTemplateResponse> listVersions(String code);

    List<PromptTemplateResponse> listAll();

    void delete(Long id);

    PromptTemplate getActiveEntity(String code);

    PromptTemplate getEntity(String code, String version);
}
