package com.duoq.medlearn.ai.dto.request;

import com.duoq.medlearn.ai.dto.response.AiMessage;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @NotEmpty(message = "At least one message is required")
    private List<AiMessage> messages;

    private String model;

    private Double temperature;

    private Integer maxTokens;

    @Builder.Default
    private boolean stream = false;

    private String promptTemplateCode;

    private String userId;
}
