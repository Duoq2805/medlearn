package com.duoq.medlearn.ai.dto.request;

import com.duoq.medlearn.ai.enums.SummaryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to generate an AI summary")
public class SummaryRequest {

    @NotNull(message = "Summary type is required")
    @Schema(description = "Type of summary to generate", example = "STUDENT")
    private SummaryType summaryType;

    @Schema(description = "Optional custom model override")
    private String model;

    @Schema(description = "Optional custom temperature override")
    private Double temperature;
}
