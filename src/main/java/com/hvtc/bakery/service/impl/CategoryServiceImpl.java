package com.hvtc.bakery.service.impl;

import com.hvtc.bakery.dto.CategoryDto;
import com.hvtc.bakery.entity.Category;
import com.hvtc.bakery.exception.BadRequestException;
import com.hvtc.bakery.exception.ResourceNotFoundException;
import com.hvtc.bakery.repository.CategoryRepository;
import com.hvtc.bakery.service.CategoryService;
import com.hvtc.bakery.service.FileStorageService;
import com.hvtc.bakery.util.SlugUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAllActive() {
        return categoryRepository.findByStatusTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục ID: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục slug: " + slug));
    }

    @Override
    @Transactional
    public Category saveCategory(CategoryDto dto) {
        String slug = dto.getSlug() != null && !dto.getSlug().isBlank()
                ? SlugUtils.toSlug(dto.getSlug())
                : SlugUtils.toSlug(dto.getName());

        if (categoryRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis() % 1000;
        }

        String imagePath = dto.getImage();
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            imagePath = fileStorageService.storeFile(dto.getImageFile(), "categories");
        }

        Category category = Category.builder()
                .name(dto.getName().trim())
                .slug(slug)
                .description(dto.getDescription())
                .image(imagePath)
                .status(dto.isStatus())
                .build();

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category updateCategory(Long id, CategoryDto dto) {
        Category category = findById(id);

        category.setName(dto.getName().trim());
        category.setDescription(dto.getDescription());
        category.setStatus(dto.isStatus());

        if (dto.getSlug() != null && !dto.getSlug().isBlank()) {
            String newSlug = SlugUtils.toSlug(dto.getSlug());
            if (!newSlug.equals(category.getSlug()) && categoryRepository.existsBySlug(newSlug)) {
                throw new BadRequestException("Slug danh mục đã tồn tại!");
            }
            category.setSlug(newSlug);
        }

        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            String imagePath = fileStorageService.storeFile(dto.getImageFile(), "categories");
            category.setImage(imagePath);
        }

        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findById(id);
        categoryRepository.delete(category);
    }
}
