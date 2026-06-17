package com.duoq.medlearn.domain.dto.disease;

import com.duoq.medlearn.domain.dto.section.DiseaseSectionDTO;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionDTO;
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
