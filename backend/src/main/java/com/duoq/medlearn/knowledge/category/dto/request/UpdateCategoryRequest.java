package com.duoq.medlearn.domain.dto.category;

import lombok.Data;

@Data
public class UpdateCategoryRequest {
    private String name;
    private String description;
}
