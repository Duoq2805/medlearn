package com.duoq.medlearn.mapper;

import com.duoq.medlearn.domain.dto.category.CategoryResponse;
import com.duoq.medlearn.domain.entity.Category;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
}
