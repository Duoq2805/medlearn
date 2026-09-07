package com.duoq.medlearn.knowledge.disease.dto.request;

import com.duoq.medlearn.knowledge.disease.enums.DiseaseStatus;
import lombok.Data;

import java.util.List;

@Data
public class DiseaseSearchRequest {

    private String keyword;
    private Long categoryId;
    private List<Long> symptomIds;
    private DiseaseStatus status;
}
