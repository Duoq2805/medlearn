package com.duoq.medlearn.mapper;

import com.duoq.medlearn.domain.dto.casestudy.CaseStudyDetailResponse;
import com.duoq.medlearn.domain.dto.casestudy.CaseStudySummaryProjection;
import com.duoq.medlearn.domain.entity.CaseStudy;
import com.duoq.medlearn.domain.entity.Symptom;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(config = MapStructConfig.class)
public interface CaseStudyMapper {
    CaseStudySummaryProjection toCaseStudySummaryProjection(CaseStudy caseStudy);

    @Mapping(target = "createdBy", expression = "java(caseStudy.getCreatedBy() != null ? caseStudy.getCreatedBy().getId() : null)")
    @Mapping(target = "symptomIds", expression = "java(mapSymptomIds(caseStudy.getSymptoms()))")
    CaseStudyDetailResponse toCaseStudyDetailResponse(CaseStudy caseStudy);

    default Set<Long> mapSymptomIds(Set<Symptom> symptoms) {
        if (symptoms == null) {
            return Set.of();
        }
        return symptoms.stream().map(Symptom::getId).collect(Collectors.toSet());
    }
}
