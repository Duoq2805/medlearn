package com.duoq.medlearn.draft.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Value;

@Value
public class DraftReviewRequest {
    @NotBlank
    String action;
    String note;
}
