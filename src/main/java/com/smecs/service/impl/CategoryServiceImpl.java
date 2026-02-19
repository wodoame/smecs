package com.smecs.service.impl;

import com.smecs.dto.CategoryDTO;
import com.smecs.dto.CategoryQuery;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.entity.Category;
import com.smecs.exception.CategoryInUseException;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.service.CategoryService;
import com.smecs.repository.CategoryRepository;
import com.smecs.repository.CategorySpecification;
import com.smecs.util.PaginationUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryCacheService categoryCacheService;

    @Autowired
    public CategoryServiceImpl(CategoryRepository categoryRepository,
            CategoryCacheService categoryCacheService) {
        this.categoryRepository = categoryRepository;
        this.categoryCacheService = categoryCacheService;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {
        Category category = new Category();
        return getCategoryDTO(categoryDTO, category);
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
            dto.setRelatedImageUrls(categoryRepository.findTop5ProductImageUrlsByCategoryId(id));
        }

        return dto;
    }

    @Override
    public PagedResponseDTO<CategoryDTO> getCategories(CategoryQuery query, Pageable pageable) {
        String name = query != null ? query.getName() : null;
        String description = query != null ? query.getDescription() : null;
        boolean includeRelatedImages = query != null && query.isIncludeRelatedImages();

        Specification<Category> specification = buildSpecification(name, description);
        Page<Category> categoryPage = categoryRepository.findAll(specification, pageable);

        List<CategoryDTO> content = categoryPage.getContent().stream().map(category -> {
            CategoryDTO dto = new CategoryDTO();
            dto.setCategoryId(category.getId().intValue());
            dto.setCategoryName(category.getName());
            dto.setDescription(category.getDescription());
            dto.setImageUrl(category.getImageUrl());
            
            if (includeRelatedImages) {
                dto.setRelatedImageUrls(
                    categoryRepository.findTop5ProductImageUrlsByCategoryId(category.getId())
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
    public PagedResponseDTO<CategoryDTO> getCategories(CategoryQuery query) {
        CategoryQuery normalized = normalize(query);
        int page = normalized.currentPage();
        int size = normalized.currentSize();
        String sort = normalized.sortOrDefault();
        String searchTerm = normalized.searchTerm();

        Optional<PagedResponseDTO<CategoryDTO>> cached = categoryCacheService.getSearchResults(searchTerm, page, size, sort,
                normalized.isIncludeRelatedImages());
        if (cached.isPresent()) {
            return cached.get();
        }

        Sort sortSpec = PaginationUtils.parseSort(sort);
        Pageable pageRequest = PageRequest.of(page - 1, size, sortSpec);

        PagedResponseDTO<CategoryDTO> data = getCategories(
                normalized,
                pageRequest);

        categoryCacheService.putSearchResults(searchTerm, page, size, sort,
                normalized.isIncludeRelatedImages(), data);

        return data;
    }

    private CategoryQuery normalize(CategoryQuery query) {
        return query != null ? query : CategoryQuery.builder().build();
    }

    @Override
    public CategoryDTO updateCategory(Long id, CategoryDTO categoryDTO) {
        Category category = categoryRepository.findById(id).orElseThrow();
        return getCategoryDTO(categoryDTO, category);
    }

    @NonNull
    private CategoryDTO getCategoryDTO(CategoryDTO categoryDTO, Category category) {
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
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }

        long productCount = categoryRepository.countProductsByCategoryId(id);
        if (productCount > 0) {
            throw new CategoryInUseException(
                "Cannot delete category because it contains " + productCount +
                " product(s). Please remove or reassign all products first."
            );
        }

        categoryRepository.deleteById(id);
        categoryCacheService.invalidateById(id);
        categoryCacheService.invalidateAllList();
    }

    private Specification<Category> buildSpecification(String name, String description) {
        return CategorySpecification.filterByCriteria(name, description);
    }
}
