package com.duoq.medlearn.draft.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class DraftReviewRequest {
    @NotBlank
    String action;
    String note;
}
