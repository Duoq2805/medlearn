package com.duoq.medlearn.knowledge.category.service;

import com.duoq.medlearn.knowledge.category.dto.response.CategoryResponse;
import com.duoq.medlearn.knowledge.category.dto.request.CreateCategoryRequest;
import com.duoq.medlearn.knowledge.category.dto.request.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryResponse> getAllCategories();

    CategoryResponse getCategoryById(Long id);

    CategoryResponse getCategoryBySlug(String slug);

    CategoryResponse createCategory(CreateCategoryRequest request);

    CategoryResponse updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);
}
