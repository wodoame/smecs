package com.smecs.service.impl;

import com.smecs.dto.CategoryDTO;
import com.smecs.entity.Category;
import com.smecs.repository.CategoryRepository;
import com.smecs.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getCategoryName());
        // No description field in entity, skip if not present
        category = categoryRepository.save(category);
        CategoryDTO result = new CategoryDTO();
        result.setCategoryId(category.getId().intValue());
        result.setCategoryName(category.getName());
        // No description field in entity, skip if not present
        return result;
    }

    @Override
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow();
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getId().intValue());
        dto.setCategoryName(category.getName());
        // No description field in entity, skip if not present
        return dto;
    }

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream().map(category -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setCategoryId(category.getId().intValue());
            dto.setCategoryName(category.getName());
            // No description field in entity, skip if not present
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(categoryDTO.getCategoryName());
        // No description field in entity, skip if not present
        category = categoryRepository.save(category);
        CategoryDTO result = new CategoryDTO();
        result.setCategoryId(category.getId().intValue());
        result.setCategoryName(category.getName());
        // No description field in entity, skip if not present
        return result;
    }

    @Override
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}
