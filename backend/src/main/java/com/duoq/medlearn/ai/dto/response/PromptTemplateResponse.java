package com.duoq.medlearn.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String systemPrompt;
    private String userPromptTemplate;
    private String version;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private List<String> requiredVariables;
    private boolean active;
    private String status;
}
