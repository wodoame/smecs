package com.smecs.service;

import com.smecs.dto.ProductDTO;
import com.smecs.dto.PagedResponseDTO;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO getProductById(Long id);
    PagedResponseDTO<ProductDTO> getProducts(String name, String description, Long categoryId, Pageable pageable);
    ProductDTO updateProduct(Long id, ProductDTO productDTO);
    void deleteProduct(Long id);
}
