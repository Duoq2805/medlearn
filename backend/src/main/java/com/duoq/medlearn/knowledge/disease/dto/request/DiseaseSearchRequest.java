package com.duoq.medlearn.domain.dto.disease;

import com.duoq.medlearn.domain.enums.DiseaseStatus;
import lombok.Data;

import java.util.List;

@Data
public class DiseaseSearchRequest {

    private String keyword;
    private Long categoryId;
    private List<Long> symptomIds;
    private DiseaseStatus status;
}
