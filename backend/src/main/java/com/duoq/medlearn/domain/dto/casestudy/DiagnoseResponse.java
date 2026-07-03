package com.duoq.medlearn.domain.dto.casestudy;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DiagnoseResponse {
    private boolean correct;
    private String feedback;
}
