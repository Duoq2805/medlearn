package com.duoq.medlearn.document.mapper;

import com.duoq.medlearn.document.dto.response.DocumentChunkResponse;
import com.duoq.medlearn.document.dto.response.DocumentResponse;
import com.duoq.medlearn.document.entity.Document;
import com.duoq.medlearn.document.entity.DocumentChunk;
import com.duoq.medlearn.common.mapper.MapStructConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = MapStructConfig.class)
public interface DocumentMapper {

    @Mapping(target = "createdBy", source = "createdBy.id")
    DocumentResponse toResponse(Document document);

    DocumentChunkResponse toChunkResponse(DocumentChunk chunk);
}
