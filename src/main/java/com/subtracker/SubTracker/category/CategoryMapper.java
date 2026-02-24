package com.subtracker.SubTracker.category;


import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public CategoryEntity requestToEntity(CategoryRequest categoryRequest) {
        CategoryEntity categoryEntity = new CategoryEntity();
        categoryEntity.setName(categoryRequest.getName());
        return categoryEntity;
    }

    public CategoryResponse entityToResponse(CategoryEntity categoryEntity) {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(categoryEntity.getId());
        categoryResponse.setName(categoryEntity.getName());
        return categoryResponse;
    }
}
