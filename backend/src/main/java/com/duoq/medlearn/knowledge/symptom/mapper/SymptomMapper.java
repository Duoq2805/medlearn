package com.duoq.medlearn.knowledge.symptom.mapper;

import com.duoq.medlearn.common.mapper.MapStructConfig;
import com.duoq.medlearn.knowledge.symptom.dto.response.SymptomResponse;
import com.duoq.medlearn.knowledge.symptom.entity.Symptom;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface SymptomMapper {
    SymptomResponse toSymptomResponse(Symptom symptom);
}
