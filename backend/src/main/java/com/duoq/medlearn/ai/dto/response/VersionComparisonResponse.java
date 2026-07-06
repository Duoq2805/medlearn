package com.duoq.medlearn.ai.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VersionComparisonResponse {
    private String code;
    private String versionA;
    private String versionB;
    private String systemPromptA;
    private String systemPromptB;
    private String userPromptTemplateA;
    private String userPromptTemplateB;
    private Map<String, Object> diff;
}
