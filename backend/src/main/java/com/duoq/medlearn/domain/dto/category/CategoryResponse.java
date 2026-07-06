package com.duoq.medlearn.domain.dto.category;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * Data Transfer Object for Category
 */
@Data
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private OffsetDateTime createdAt;
}