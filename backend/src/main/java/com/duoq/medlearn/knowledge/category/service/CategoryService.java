package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.category.CategoryResponse;
import com.duoq.medlearn.domain.dto.category.CreateCategoryRequest;
import com.duoq.medlearn.domain.dto.category.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse getCategoryBySlug(String slug);

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);
}
