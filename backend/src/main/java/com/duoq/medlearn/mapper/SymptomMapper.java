package com.duoq.medlearn.mapper;

import com.duoq.medlearn.domain.dto.symptom.SymptomResponse;
import com.duoq.medlearn.domain.entity.Symptom;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface SymptomMapper {
    SymptomResponse toSymptomResponse(Symptom symptom);
}
