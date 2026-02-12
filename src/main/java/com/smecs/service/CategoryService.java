package com.smecs.service;

import com.smecs.dto.CategoryDTO;
import com.smecs.dto.PagedResponseDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO getCategoryById(Long id, boolean includeRelatedImages);

    PagedResponseDTO<CategoryDTO> getCategories(String name, String description, Pageable pageable,
            boolean includeRelatedImages);

    PagedResponseDTO<CategoryDTO> getCategories(String query, int page, int size, String sort, boolean relatedImages);

    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);

    void deleteCategory(Long id);
}
