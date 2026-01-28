package com.smecs.controller;

import com.smecs.dto.ProductDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
    public ResponseDTO<List<ProductDTO>> getAllProducts() {
        return new ResponseDTO<>("success", "Products retrieved", productService.getAllProducts());
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
