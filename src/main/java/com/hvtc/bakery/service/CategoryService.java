package com.hvtc.bakery.service;

import com.hvtc.bakery.dto.CategoryDto;
import com.hvtc.bakery.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> findAllActive();
    List<Category> findAll();
    Category findById(Long id);
    Category findBySlug(String slug);
    Category saveCategory(CategoryDto dto);
    Category updateCategory(Long id, CategoryDto dto);
    void deleteCategory(Long id);
}
