package com.smecs.service;

import com.smecs.dto.CategoryDTO;
import com.smecs.dto.CategoryQuery;
import com.smecs.dto.PagedResponseDTO;
import org.springframework.data.domain.Pageable;

public interface CategoryService {
    CategoryDTO createCategory(CategoryDTO categoryDTO);

    CategoryDTO getCategoryById(Long id, boolean includeRelatedImages);

    PagedResponseDTO<CategoryDTO> getCategories(CategoryQuery query, Pageable pageable);

    PagedResponseDTO<CategoryDTO> getCategories(CategoryQuery query);

    CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO);

    void deleteCategory(Long id);
}
