package com.smecs.controller;

import com.smecs.dto.CategoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.service.CategoryService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService service) {
        this.categoryService = service;
    }

    @GetMapping
    public ResponseDTO<PagedResponseDTO<CategoryDTO>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false, defaultValue = "false") boolean includeRelatedImages,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "5") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Sort sortSpec = parseSort(sort);
        PageRequest pageRequest = PageRequest.of(page, size, sortSpec);
        PagedResponseDTO<CategoryDTO> data = categoryService.getCategories(name, description, includeRelatedImages, pageRequest);
        return new ResponseDTO<>("success", "Categories retrieved", data);
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "name");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 ? Sort.Direction.fromString(parts[1].trim()) : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }

    @GetMapping("/{id}")
    public ResponseDTO<CategoryDTO> get(@PathVariable Long id, @RequestParam(required = false, defaultValue = "false") boolean includeRelatedImages) {
        return new ResponseDTO<>("success", "Category found", categoryService.getCategoryById(id, includeRelatedImages));
    }

    @PostMapping
    public ResponseDTO<CategoryDTO> create(@Valid @RequestBody CategoryDTO dto) {
        return new ResponseDTO<>("success", "Category created", categoryService.createCategory(dto));
    }

    @PutMapping("/{id}")
    public ResponseDTO<CategoryDTO> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        return new ResponseDTO<>("success", "Category updated", categoryService.updateCategory(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseDTO<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return new ResponseDTO<>("success", "Category deleted", null);
    }
}
