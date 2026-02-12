package com.smecs.controller;

import com.smecs.annotation.RequireRole;
import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
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
    @RequireRole("admin")
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
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "8") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        PagedResponseDTO<ProductDTO> results = productService.getProducts(query, query, categoryId, page, size, sort);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Products retrieved", results));
    }


    @PutMapping("/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<ProductDTO>> updateProduct(@PathVariable Long id, @RequestBody CreateProductRequestDTO request) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Product updated", productService.updateProduct(id, request)));
    }

    @DeleteMapping("/{id}")
    @RequireRole("admin")
    public ResponseEntity<ResponseDTO<Void>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(new ResponseDTO<>("success", "Product deleted", null));
    }
}
