package com.duoq.medlearn.knowledge.disease.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagnoseResponse {
    private boolean correct;
    private String feedback;
}
