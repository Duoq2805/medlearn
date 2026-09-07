package com.duoq.medlearn.ai.dto.response;

import com.duoq.medlearn.ai.enums.SummaryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "AI-generated disease summary")
public class SummaryResponse {

    @Schema(description = "Summary ID")
    private Long id;

    @Schema(description = "Disease ID")
    private Long diseaseId;

    @Schema(description = "Disease name")
    private String diseaseName;

    @Schema(description = "Disease version ID used for generation")
    private Long diseaseVersionId;

    @Schema(description = "Summary type")
    private SummaryType summaryType;

    @Schema(description = "Version number of this summary (increments on regenerate)")
    private Integer version;

    @Schema(description = "Generated summary content")
    private String content;

    @Schema(description = "AI model used")
    private String model;

    @Schema(description = "AI provider used")
    private String provider;

    @Schema(description = "Prompt tokens consumed")
    private int promptTokens;

    @Schema(description = "Completion tokens consumed")
    private int completionTokens;

    @Schema(description = "Total tokens consumed")
    private int totalTokens;

    @Schema(description = "Latency in milliseconds")
    private Integer latencyMs;

    @Schema(description = "Created timestamp")
    private OffsetDateTime createdAt;
}
