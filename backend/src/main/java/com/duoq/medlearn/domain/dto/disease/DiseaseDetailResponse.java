package com.duoq.medlearn.domain.dto.disease;

import com.duoq.medlearn.domain.dto.section.DiseaseSectionResponse;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionResponse;
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
