package com.smecs.service.impl;

import com.smecs.dto.CategoryDTO;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.entity.Category;
import com.smecs.repository.CategoryRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.CategorySpecification;
import com.smecs.service.CategoryService;
import com.smecs.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final CacheService<CategoryDTO, Long> categoryCacheService;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository, ProductRepository productRepository, CacheService<CategoryDTO, Long> categoryCacheService) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.categoryCacheService = categoryCacheService;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        category.setName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());
        category.setImageUrl(categoryDTO.getImageUrl());
        category = categoryRepository.save(category);
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
    public CategoryDTO getCategoryById(Long id, boolean includeRelatedImages) {
        Category category = categoryRepository.findById(id).orElseThrow();
        CategoryDTO dto = new CategoryDTO();
        dto.setCategoryId(category.getId().intValue());
        dto.setCategoryName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setImageUrl(category.getImageUrl());
        if (includeRelatedImages) {
            dto.setRelatedImageUrls(productRepository.findTop5ImagesByCategoryId(id));
        }
        return dto;
    }

    @Override
    public PagedResponseDTO<CategoryDTO> getCategories(String name, String description, boolean includeRelatedImages, Pageable pageable) {
        Specification<Category> spec = CategorySpecification.filterByCriteria(name, description);
        Page<Category> categoryPage = categoryRepository.findAll(spec, pageable);

        List<CategoryDTO> content = categoryPage.getContent().stream().map(category -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setCategoryId(category.getId().intValue());
            dto.setCategoryName(category.getName());
            dto.setDescription(category.getDescription());
            dto.setImageUrl(category.getImageUrl());
            if (includeRelatedImages) {
                dto.setRelatedImageUrls(productRepository.findTop5ImagesByCategoryId(category.getId()));
            }
            return dto;
        }).collect(Collectors.toList());

        PagedResponseDTO<CategoryDTO> pagedResponse = new PagedResponseDTO<>();
        PageMetadataDTO pageMetadata = new PageMetadataDTO();
        pagedResponse.setContent(content);
        pageMetadata.setPage(categoryPage.getNumber());
        pageMetadata.setSize(categoryPage.getSize());
        pageMetadata.setTotalElements(categoryPage.getTotalElements());
        pageMetadata.setTotalPages(categoryPage.getTotalPages());
        pageMetadata.setFirst(categoryPage.isFirst());
        pageMetadata.setLast(categoryPage.isLast());
        pageMetadata.setEmpty(categoryPage.isEmpty());
        pageMetadata.setHasNext(categoryPage.hasNext());
        pageMetadata.setHasPrevious(categoryPage.hasPrevious());
        pagedResponse.setPage(pageMetadata);
        return pagedResponse;
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id).orElseThrow();
        category.setName(categoryDTO.getCategoryName());
        category.setDescription(categoryDTO.getDescription());
        category.setImageUrl(categoryDTO.getImageUrl());
        category = categoryRepository.save(category);
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
        categoryRepository.deleteById(id);
        categoryCacheService.invalidateById(id);
        categoryCacheService.invalidateAllList();
    }
}
