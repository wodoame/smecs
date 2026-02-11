package com.smecs.controller;

import com.smecs.annotation.RequireRole;
import com.smecs.dto.CategoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.service.CategoryService;
import com.smecs.service.impl.CategoryCacheService;
import com.smecs.util.PaginationUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Optional;

@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryCacheService categoryCacheService;

    public CategoryController(CategoryService service, CategoryCacheService cacheService) {
        this.categoryService = service;
        this.categoryCacheService = cacheService;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<PagedResponseDTO<CategoryDTO>>> list(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false, defaultValue = "false") boolean relatedImages) {
        Optional<PagedResponseDTO<CategoryDTO>> cached = categoryCacheService.getSearchResults(query, page, size, sort,
                relatedImages);
        if (cached.isPresent()) {
            return ResponseEntity.ok(new ResponseDTO<>("success", "Categories retrieved", cached.get()));
        }

        Sort sortSpec = PaginationUtils.parseSort(sort);
        // Use 0-based page index for Spring Data
        PageRequest pageRequest = PageRequest.of(page - 1, size, sortSpec);
        PagedResponseDTO<CategoryDTO> data = categoryService.getCategories(query, query, pageRequest, relatedImages);

        categoryCacheService.putSearchResults(query, page, size, sort, relatedImages, data);

        return ResponseEntity.ok(new ResponseDTO<>("success", "Categories retrieved", data));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<CategoryDTO>> get(@PathVariable Long id,
            @RequestParam(required = false, defaultValue = "false") boolean relatedImages) {
        return ResponseEntity
                .ok(new ResponseDTO<>("success", "Category found", categoryService.getCategoryById(id, relatedImages)));
    }

    @PostMapping
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<CategoryDTO>> create(@Valid @RequestBody CategoryDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Category created", categoryService.createCategory(dto)));
    }

    @PutMapping("/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<CategoryDTO>> update(@PathVariable Long id, @Valid @RequestBody CategoryDTO dto) {
        return ResponseEntity
                .ok(new ResponseDTO<>("success", "Category updated", categoryService.updateCategory(id, dto)));
    }

    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<Void>> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Category deleted", null));
    }
}
