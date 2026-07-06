package com.duoq.medlearn.draft.mapper;

import com.duoq.medlearn.draft.dto.DiseaseDraftResponse;
import com.duoq.medlearn.draft.dto.DiseaseDraftSectionResponse;
import com.duoq.medlearn.draft.entity.DiseaseDraft;
import com.duoq.medlearn.draft.entity.DiseaseDraftSection;
import com.duoq.medlearn.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface DraftMapper {

    @Mapping(target = "diseaseId", source = "disease.id")
    @Mapping(target = "diseaseName", source = "disease.name")
    @Mapping(target = "sourceDocumentId", source = "sourceDocument.id")
    @Mapping(target = "createdBy", source = "createdBy.id")
    @Mapping(target = "reviewedBy", source = "reviewedBy.id")
    @Mapping(target = "sections", ignore = true)
    DiseaseDraftResponse toResponse(DiseaseDraft draft);

    DiseaseDraftSectionResponse toSectionResponse(DiseaseDraftSection section);

    List<DiseaseDraftSectionResponse> toSectionResponseList(List<DiseaseDraftSection> sections);
}
