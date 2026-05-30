package com.duoq.medlearn.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionTypeDTO {
    private Integer id;
    private String name;
    private String description;
}
