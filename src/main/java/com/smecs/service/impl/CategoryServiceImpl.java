package com.smecs.service.impl;

import com.smecs.dao.CategoryDAO;
import com.smecs.dao.ProductDAO;
import com.smecs.dto.CategoryDTO;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.entity.Category;
import com.smecs.entity.Product;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.service.CacheService;
import com.smecs.service.CategoryService;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryDAO categoryDAO;
    private final ProductDAO productDAO;
    private final CacheService<CategoryDTO, Long> categoryCacheService;

    @Autowired
    public CategoryServiceImpl(CategoryDAO categoryDAO, ProductDAO productDAO,
            CacheService<CategoryDTO, Long> categoryCacheService) {
        this.categoryDAO = categoryDAO;
        this.productDAO = productDAO;
        this.categoryCacheService = categoryCacheService;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        return getCategoryDTO(categoryDTO, category);
    }

    @Override
    public CategoryDTO getCategoryById(Long id, boolean includeRelatedImages) {
        Category category = categoryDAO.findById(id).orElseThrow();
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getId().intValue());
        dto.setCategoryName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());

        if (includeRelatedImages) {
            dto.setRelatedImageUrls(
                    productDAO.findTop5ByCategory(id).stream()
                            .map(com.smecs.entity.Product::getImageUrl)
                            .collect(Collectors.toList()));
        }

        return dto;
    }

    @Override
    public PagedResponseDTO<CategoryDTO> getCategories(String name, String description, Pageable pageable, boolean includeRelatedImages) {
        // Use native SQL implementation from DAO for low-level database operations
        Page<Category> categoryPage = categoryDAO.searchCategories(name, description, pageable);

        List<CategoryDTO> content = categoryPage.getContent().stream().map(category -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setCategoryId(category.getId().intValue());
            dto.setCategoryName(category.getName());
            dto.setDescription(category.getDescription());
            dto.setImageUrl(category.getImageUrl());
            
            if (includeRelatedImages) {
                dto.setRelatedImageUrls(
                    productDAO.findTop5ByCategory(category.getId()).stream()
                        .map(Product::getImageUrl)
                        .collect(Collectors.toList())
                );
            }
            
            return dto;
        }).collect(Collectors.toList());

        PagedResponseDTO<CategoryDTO> pagedResponse = new PagedResponseDTO<>();
        pagedResponse.setContent(content);
        pagedResponse.setPage(PageMetadataDTO.from(categoryPage));

        return pagedResponse;
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryDAO.findById(id).orElseThrow();
        return getCategoryDTO(categoryDTO, category);
    }

    @NonNull
    private CategoryDTO getCategoryDTO(CategoryDTO categoryDTO, Category category) {
        category.setName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());
        category.setImageUrl(categoryDTO.getImageUrl());
        category = categoryDAO.save(category);
        CategoryDTO result = new CategoryDTO();
        result.setCategoryId(category.getId().intValue());
        result.setCategoryName(category.getName());
        result.setDescription(category.getDescription());
        result.setImageUrl(category.getImageUrl());
        categoryCacheService.put(result);
        categoryCacheService.invalidateAllList();
        return result;
    }

    @Override
    public void deleteCategory(Long id) {
        if (!categoryDAO.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryDAO.deleteById(id);
        categoryCacheService.invalidateById(id);
        categoryCacheService.invalidateAllList();
    }
}
