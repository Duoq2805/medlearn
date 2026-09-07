package com.duoq.medlearn.knowledge.disease.mapper;

import com.duoq.medlearn.common.mapper.MapStructConfig;
import com.duoq.medlearn.knowledge.disease.entity.Disease;
import com.duoq.medlearn.knowledge.disease.entity.DiseaseSection;
import com.duoq.medlearn.knowledge.version.entity.DiseaseVersion;
import com.duoq.medlearn.knowledge.disease.dto.response.DiseaseDetailResponse;
import com.duoq.medlearn.knowledge.disease.dto.response.DiseaseResponse;
import com.duoq.medlearn.knowledge.section.dto.response.DiseaseSectionResponse;
import com.duoq.medlearn.knowledge.version.dto.response.DiseaseVersionResponse;
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

    default DiseaseDetailResponse toDiseaseDetailResponse(Disease disease) {
        if (disease == null) return null;
        return DiseaseDetailResponse.builder()
                .disease(toDiseaseResponse(disease))
                .currentVersion(disease.getCurrentVersion() != null ? toDiseaseVersionResponse(disease.getCurrentVersion()) : null)
                .build();
    }

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "slug", source = "slug")
    @Mapping(target = "updatedAt", source = "updatedAt")
    com.duoq.medlearn.knowledge.disease.dto.projection.DiseaseSummaryProjection toDiseaseSummaryProjection(Disease disease);

    default com.duoq.medlearn.knowledge.disease.enums.DiseaseStatus mapStatus(String versionStatus) {
        return switch (versionStatus) {
            case "APPROVED" -> com.duoq.medlearn.knowledge.disease.enums.DiseaseStatus.APPROVED;
            case "PENDING_REVIEW" -> com.duoq.medlearn.knowledge.disease.enums.DiseaseStatus.PENDING_REVIEW;
            case "ARCHIVED" -> com.duoq.medlearn.knowledge.disease.enums.DiseaseStatus.ARCHIVED;
            default -> com.duoq.medlearn.knowledge.disease.enums.DiseaseStatus.DRAFT;
        };
    }
}
