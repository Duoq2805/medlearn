package com.duoq.medlearn.knowledge.section.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SectionOrderRequest {

    @NotNull(message = "Section id is required")
    private Long sectionId;

    @NotNull(message = "Order index is required")
    private Integer orderIndex;
}
