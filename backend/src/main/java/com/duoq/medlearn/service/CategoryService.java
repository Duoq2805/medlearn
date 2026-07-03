package com.duoq.medlearn.service;

import com.duoq.medlearn.domain.dto.category.CategoryDTO;
import com.duoq.medlearn.domain.dto.category.CreateCategoryRequest;
import com.duoq.medlearn.domain.dto.category.UpdateCategoryRequest;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();

    CategoryDTO getCategoryById(Long id);

    CategoryDTO getCategoryBySlug(String slug);

    CategoryDTO createCategory(CreateCategoryRequest request);

    CategoryDTO updateCategory(Long id, UpdateCategoryRequest request);

    void deleteCategory(Long id);
}
