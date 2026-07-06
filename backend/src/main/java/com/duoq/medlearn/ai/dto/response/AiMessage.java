package com.duoq.medlearn.ai.dto.response;

import com.duoq.medlearn.ai.dto.enums.AiRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiMessage {
    private AiRole role;
    private String content;
}
