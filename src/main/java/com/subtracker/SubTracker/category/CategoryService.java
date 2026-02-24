package com.subtracker.SubTracker.category;


import com.subtracker.SubTracker.common.PageMapper;
import com.subtracker.SubTracker.common.PageResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@Slf4j
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private PageMapper pageMapper;

    public CategoryResponse createCategory(CategoryRequest categoryRequest) {

        CategoryEntity categoryEntity = categoryMapper.requestToEntity(categoryRequest);
        categoryRepository.save(categoryEntity);
        return categoryMapper.entityToResponse(categoryEntity);
    }


    //Get Category By category - id
    public CategoryResponse getCategoryById(Long categoryId) {

        CategoryEntity categoryEntity = categoryRepository.findById(categoryId).
                                        orElseThrow(()->new NoSuchElementException("Category not found"));
        return categoryMapper.entityToResponse(categoryEntity);
    }


    //Get All Categories
    public PageResponseDto<CategoryResponse> getAll(Pageable pageable) {
        Page<CategoryEntity> categoryEntities = categoryRepository.findAll(pageable);
        Page<CategoryResponse> categoryResponses = categoryEntities.map(category->categoryMapper.entityToResponse(category));
        return pageMapper.pageToPageDto(categoryResponses);
    }

    //Update Category
    public CategoryResponse updateCategory(Long categoryId, CategoryRequest categoryRequest) {

        CategoryEntity categoryEntity = categoryRepository.findById(categoryId)
                .orElseThrow(()->new NoSuchElementException("Category not found"));
        categoryEntity.setName(categoryRequest.getName());
        return categoryMapper.entityToResponse(categoryRepository.save(categoryEntity));
    }


    public void deleteCategory(Long categoryId) {
        if (categoryRepository.findById(categoryId).isPresent()) {
            categoryRepository.deleteById(categoryId);
        }
    }
}
