package com.subtracker.SubTracker.category;

import com.subtracker.SubTracker.common.PageResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@Slf4j
public class CategoryController {


    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest categoryRequest) {
        return new ResponseEntity<>(categoryService.createCategory(categoryRequest), HttpStatus.OK);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long categoryId){

        CategoryResponse categoryResponse = categoryService.getCategoryById(categoryId);
        if(categoryResponse == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }


    //Get All Categories
    @GetMapping
    public ResponseEntity<PageResponseDto<CategoryResponse>> getAllCategories(@PageableDefault(page = 0, size = 10) Pageable pageable){

        return new ResponseEntity<>(categoryService.getAll(pageable),HttpStatus.OK);
    }


    //Update Category
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long categoryId, @RequestBody CategoryRequest categoryRequest){

        CategoryResponse categoryResponse = categoryService.updateCategory(categoryId,categoryRequest);
        if(categoryResponse == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(categoryResponse, HttpStatus.OK);
    }

    //Delete Category
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable Long categoryId){

        try{
            categoryService.deleteCategory(categoryId);
            return new ResponseEntity<>("Deleted ",HttpStatus.OK);
        }catch(Exception e){
            log.error(e.getMessage(),e);
        }
        return new ResponseEntity<>("Cannot find Category",HttpStatus.NOT_FOUND);
    }

}
