package com.smecs.service;

import com.smecs.dto.CategoryDTO;
import com.smecs.dto.PagedResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);
    CategoryDTO getCategoryById(Long id, boolean includeRelatedImages);
    List<CategoryDTO> getAllCategories(boolean includeRelatedImages);
    PagedResponseDTO<CategoryDTO> getCategories(String name, String description, boolean includeRelatedImages, Pageable pageable);
    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);
    void deleteCategory(Long id);
}
