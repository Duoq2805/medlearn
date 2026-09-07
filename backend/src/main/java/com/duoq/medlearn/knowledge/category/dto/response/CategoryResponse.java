package com.duoq.medlearn.knowledge.category.dto.response;

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