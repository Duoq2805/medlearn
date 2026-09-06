package com.duoq.medlearn.mapper;

import com.duoq.medlearn.domain.entity.Disease;
import com.duoq.medlearn.domain.entity.DiseaseSection;
import com.duoq.medlearn.domain.entity.DiseaseVersion;
import com.duoq.medlearn.domain.dto.disease.DiseaseResponse;
import com.duoq.medlearn.domain.dto.section.DiseaseSectionResponse;
import com.duoq.medlearn.domain.dto.version.DiseaseVersionResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface DiseaseMapper {

    @Mapping(target = "categoryId", expression = "java(disease.getCategory() != null ? disease.getCategory().getId() : null)")
    @Mapping(target = "categoryName", expression = "java(disease.getCategory() != null ? disease.getCategory().getName() : null)")
    @Mapping(target = "currentVersionId", expression = "java(disease.getCurrentVersion() != null ? disease.getCurrentVersion().getId() : null)")
    @Mapping(target = "status", expression = "java(disease.getCurrentVersion() != null && disease.getCurrentVersion().getStatus() != null ? mapStatus(disease.getCurrentVersion().getStatus().name()) : null)")
    DiseaseResponse toDiseaseResponse(Disease disease);

    @Mapping(target = "diseaseId", expression = "java(version.getDisease() != null ? version.getDisease().getId() : null)")
    @Mapping(target = "createdById", expression = "java(version.getCreatedBy() != null ? version.getCreatedBy().getId() : null)")
    @Mapping(target = "reviewedById", expression = "java(version.getReviewedBy() != null ? version.getReviewedBy().getId() : null)")
    DiseaseVersionResponse toDiseaseVersionResponse(DiseaseVersion version);

    @Mapping(target = "diseaseVersionId", expression = "java(section.getDiseaseVersion() != null ? section.getDiseaseVersion().getId() : null)")
    @Mapping(target = "sectionTypeId", expression = "java(section.getSectionType() != null ? section.getSectionType().getId() : null)")
    @Mapping(target = "sectionTypeName", expression = "java(section.getSectionType() != null ? section.getSectionType().getName() : null)")
    DiseaseSectionResponse toDiseaseSectionResponse(DiseaseSection section);

    default com.duoq.medlearn.domain.enums.DiseaseStatus mapStatus(String versionStatus) {
        return switch (versionStatus) {
            case "APPROVED" -> com.duoq.medlearn.domain.enums.DiseaseStatus.APPROVED;
            case "PENDING_REVIEW" -> com.duoq.medlearn.domain.enums.DiseaseStatus.PENDING_REVIEW;
            case "ARCHIVED" -> com.duoq.medlearn.domain.enums.DiseaseStatus.ARCHIVED;
            default -> com.duoq.medlearn.domain.enums.DiseaseStatus.DRAFT;
        };
    }
}
