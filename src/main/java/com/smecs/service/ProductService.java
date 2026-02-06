package com.smecs.service;

import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.PagedResponseDTO;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductDTO createProduct(CreateProductRequestDTO request);
    ProductDTO getProductById(Long id);
    PagedResponseDTO<ProductDTO> getProducts(String name, String description, Pageable pageable);
    ProductDTO updateProduct(Long id, CreateProductRequestDTO request);
    void deleteProduct(Long id);
}
