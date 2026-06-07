package com.duoq.medlearn.domain.dto.disease;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ModerationRequest {

    @Size(max = 2000, message = "Moderation note must be at most 2000 characters")
    private String note;
}
