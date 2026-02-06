package com.smecs.controller;

import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.service.ProductService;
import com.smecs.service.impl.ProductCacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;
    private final ProductCacheService productCacheService;

    @Autowired
    public ProductController(ProductService productService, ProductCacheService productCacheService) {
        this.productService = productService;
        this.productCacheService = productCacheService;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<ProductDTO>> createProduct(@Valid @RequestBody CreateProductRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Product created", productService.createProduct(request)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ResponseDTO<ProductDTO>> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Product found", productService.getProductById(id)));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<PagedResponseDTO<ProductDTO>>> searchProducts(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "8") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Optional<PagedResponseDTO<ProductDTO>> cached = productCacheService.getSearchResults(query, page, size, sort);
        if (cached.isPresent()) {
            return ResponseEntity.ok(new ResponseDTO<>("success", "Products retrieved", cached.get()));
        }

        Pageable pageable = PageRequest.of(page - 1, size, parseSort(sort));
        PagedResponseDTO<ProductDTO> results = productService.getProducts(query, query, pageable);
        productCacheService.putSearchResults(query, page, size, sort, results);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Products retrieved", results));
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

    @PutMapping("/{id}")
    public ResponseEntity<ResponseDTO<ProductDTO>> updateProduct(@PathVariable Long id, @RequestBody CreateProductRequestDTO request) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseDTO<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Product deleted", null));
    }
}
