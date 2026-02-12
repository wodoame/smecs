package com.smecs.controller;

import com.smecs.annotation.RequireRole;
import com.smecs.dto.CategoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/api/categories")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService service) {
        this.categoryService = service;
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<PagedResponseDTO<CategoryDTO>>> list(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name,asc") String sort,
            @RequestParam(required = false, defaultValue = "false") boolean relatedImages) {

        PagedResponseDTO<CategoryDTO> data = categoryService.getCategories(query, page, size, sort, relatedImages);
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
