package com.smecs.service.impl;

import com.smecs.dto.ProductDTO;
import com.smecs.entity.Product;
import com.smecs.entity.Category;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.CategoryRepository;
import com.smecs.service.ProductService;
import com.smecs.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CacheService<ProductDTO, Long> productCacheService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository, CategoryRepository categoryRepository, CacheService<ProductDTO, Long> productCacheService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.productCacheService = productCacheService;
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = new Product();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());
        Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow();
        product.setCategory(category);
        product = productRepository.save(product);
        productDTO.setId(product.getId());
        productCacheService.put(productDTO);
        productCacheService.invalidateAllList();
        return productDTO;
    }

    @Override
    public ProductDTO getProductById(Long id) {
        return productCacheService.getById(id).orElseGet(() -> {
            Product product = productRepository.findById(id).orElseThrow();
            ProductDTO dto = mapToDto(product);
            productCacheService.put(dto);
            return dto;
        });
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        return productCacheService.getAll().orElseGet(() -> {
            List<ProductDTO> products = productRepository.findAll().stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            productCacheService.putAll(products);
            return products;
        });
    }

    @Override
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        Product product = productRepository.findById(id).orElseThrow();
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());
        Category category = categoryRepository.findById(productDTO.getCategoryId()).orElseThrow();
        product.setCategory(category);
        product = productRepository.save(product);
        productDTO.setId(product.getId());
        productCacheService.put(productDTO);
        productCacheService.invalidateAllList();
        return productDTO;
    }

    @Override
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
        productCacheService.invalidateById(id);
        productCacheService.invalidateAllList();
    }

    private ProductDTO mapToDto(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());
        dto.setCategoryId(product.getCategory().getId());
        return dto;
    }
}
