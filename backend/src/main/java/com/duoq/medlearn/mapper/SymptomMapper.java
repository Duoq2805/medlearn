package com.duoq.medlearn.mapper;

import com.duoq.medlearn.domain.dto.symptom.SymptomDTO;
import com.duoq.medlearn.domain.entity.Symptom;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface SymptomMapper {
    SymptomDTO toSymptomDTO(Symptom symptom);
}
