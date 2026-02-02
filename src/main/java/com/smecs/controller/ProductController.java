package com.smecs.controller;

import com.smecs.dto.ProductDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Validated
@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseDTO<ProductDTO> createProduct(@RequestBody ProductDTO productDTO) {
        return new ResponseDTO<>("success", "Product created", productService.createProduct(productDTO));
    }

    @GetMapping("/{id}")
    public ResponseDTO<ProductDTO> getProduct(@PathVariable Long id) {
        return new ResponseDTO<>("success", "Product found", productService.getProductById(id));
    }

    @GetMapping
    public ResponseDTO<PagedResponseDTO<ProductDTO>> getAllProducts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "8") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "name,asc") String sort
    ) {
        Sort sortSpec = parseSort(sort);
        PageRequest pageRequest = PageRequest.of(page, size, sortSpec);
        PagedResponseDTO<ProductDTO> data = productService.getProducts(name, description, categoryId, pageRequest);
        return new ResponseDTO<>("success", "Products retrieved", data);
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
    public ResponseDTO<ProductDTO> updateProduct(@PathVariable Long id, @RequestBody ProductDTO productDTO) {
        return new ResponseDTO<>("success", "Product updated", productService.updateProduct(id, productDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseDTO<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return new ResponseDTO<>("success", "Product deleted", null);
    }
}
