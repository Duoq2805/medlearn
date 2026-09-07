package com.duoq.medlearn.knowledge.disease.dto.response;

import com.duoq.medlearn.knowledge.section.dto.response.DiseaseSectionResponse;
import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DiseaseDetailResponse {
    private DiseaseResponse disease;
    private DiseaseVersionResponse currentVersion;
    private List<DiseaseSectionResponse> sections;
}
