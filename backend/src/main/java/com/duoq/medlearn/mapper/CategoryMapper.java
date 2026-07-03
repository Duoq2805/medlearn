package com.duoq.medlearn.mapper;

import com.duoq.medlearn.domain.dto.category.CategoryDTO;
import com.duoq.medlearn.domain.entity.Category;
import org.mapstruct.Mapper;

@Mapper(config = MapStructConfig.class)
public interface CategoryMapper {
    CategoryDTO toCategoryDTO(Category category);
}
