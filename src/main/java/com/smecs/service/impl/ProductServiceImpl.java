package com.smecs.service.impl;

import com.smecs.dao.ProductDAO;
import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.entity.Product;
import com.smecs.service.ProductService;
import com.smecs.service.CacheService;
import com.smecs.exception.ResourceNotFoundException;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductDAO productDAO;
    private final CacheService<ProductDTO, Long> productCacheService;

    @Autowired
    public ProductServiceImpl(ProductDAO productDAO, CacheService<ProductDTO, Long> productCacheService) {
        this.productDAO = productDAO;
        this.productCacheService = productCacheService;
    }

    @Override
    public ProductDTO createProduct(CreateProductRequestDTO request) {
        Product product = new Product();
        return getProductDTO(request, product);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        return productCacheService.getById(id).orElseGet(() -> {
            Product product = productDAO.findById(id).orElseThrow();
            ProductDTO dto = mapToDto(product);
            productCacheService.put(dto);
            return dto;
        });
    }

    @Override
    public PagedResponseDTO<ProductDTO> getProducts(String name, String description, Pageable pageable) {
        // Use native SQL implementation from DAO for low-level database operations
        Page<Product> productPage = productDAO.searchProducts(name, description, pageable);

        List<ProductDTO> content = productPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        PagedResponseDTO<ProductDTO> pagedResponse = new PagedResponseDTO<>();
        pagedResponse.setContent(content);
        pagedResponse.setPage(PageMetadataDTO.from(productPage));

        return pagedResponse;
    }

    @Override
    public ProductDTO updateProduct(Long id, CreateProductRequestDTO productDTO) {
        Product product = productDAO.findById(id).orElseThrow();
        return getProductDTO(productDTO, product);
    }

    @NonNull
    private ProductDTO getProductDTO(CreateProductRequestDTO productDTO, Product product) {
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());
        product.setCategoryId(productDTO.getCategoryId());

        product = productDAO.save(product);
        ProductDTO result = mapToDto(product);
        productCacheService.put(result);
        productCacheService.invalidateAllList();
        return result;
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productDAO.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productDAO.deleteById(id);
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
        dto.setCategoryId(product.getCategoryId());
        return dto;
    }
}
