package com.duoq.medlearn.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DiseaseDetailDTO {
    private DiseaseDTO disease;
    private DiseaseVersionDTO currentVersion;
    private List<DiseaseSectionDTO> sections;
}
