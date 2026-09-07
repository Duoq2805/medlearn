package com.duoq.medlearn.knowledge.category.mapper;

import com.duoq.medlearn.common.mapper.MapStructConfig;
import com.duoq.medlearn.knowledge.category.dto.response.CategoryResponse;
import com.duoq.medlearn.knowledge.category.entity.Category;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
}
