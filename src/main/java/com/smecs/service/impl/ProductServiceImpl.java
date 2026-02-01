package com.smecs.service.impl;

import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.entity.Product;
import com.smecs.entity.Category;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.ProductSpecification;
import com.smecs.repository.CategoryRepository;
import com.smecs.service.ProductService;
import com.smecs.service.CacheService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    public PagedResponseDTO<ProductDTO> getProducts(String name, String description, Long categoryId, Pageable pageable) {
        // Don't use cache for paginated/filtered queries - they're too varied to cache effectively
        Specification<Product> spec = ProductSpecification.filterByCriteria(name, description, categoryId);
        Page<Product> productPage = productRepository.findAll(spec, pageable);

        List<ProductDTO> content = productPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        PagedResponseDTO<ProductDTO> pagedResponse = new PagedResponseDTO<>();
        PageMetadataDTO pageMetadata = new PageMetadataDTO();
        pagedResponse.setContent(content);
        pageMetadata.setPage(productPage.getNumber());
        pageMetadata.setSize(productPage.getSize());
        pageMetadata.setTotalElements(productPage.getTotalElements());
        pageMetadata.setTotalPages(productPage.getTotalPages());
        pageMetadata.setFirst(productPage.isFirst());
        pageMetadata.setLast(productPage.isLast());
        pageMetadata.setEmpty(productPage.isEmpty());
        pageMetadata.setHasNext(productPage.hasNext());
        pageMetadata.setHasPrevious(productPage.hasPrevious());
        pagedResponse.setPage(pageMetadata);
        return pagedResponse;
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
