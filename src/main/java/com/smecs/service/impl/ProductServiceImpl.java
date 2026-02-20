package com.smecs.service.impl;

import com.smecs.config.CacheConfig;
import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ProductQuery;
import com.smecs.entity.Product;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.ProductSpecification;
import com.smecs.service.ProductService;
import com.smecs.util.PaginationUtils;
import lombok.AllArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    @CachePut(value = CacheConfig.PRODUCTS_BY_ID, key = "#result.id")
    @CacheEvict(value = CacheConfig.PRODUCT_SEARCH, allEntries = true)
    public ProductDTO createProduct(CreateProductRequestDTO request) {
        Product product = new Product();
        return persistProduct(request, product);
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCTS_BY_ID, key = "#id")
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return mapToDto(product);
    }

    @Override
    @Cacheable(value = CacheConfig.PRODUCT_SEARCH, key = "T(com.smecs.service.impl.ProductServiceImpl).searchCacheKey(#query)")
    public PagedResponseDTO<ProductDTO> getProducts(ProductQuery query) {
        ProductQuery normalized = normalize(query);
        Sort sortSpec = PaginationUtils.parseSort(normalized.sortOrDefault(), "id");
        Pageable pageable = PageRequest.of(normalized.currentPage() - 1, normalized.currentSize(), sortSpec);
        return getProducts(normalized, pageable);
    }

    @Override
    public PagedResponseDTO<ProductDTO> getProducts(ProductQuery query, Pageable pageable) {
        Specification<Product> specification = buildSpecification(query);
        Page<Product> productPage = productRepository.findAll(specification, pageable);

        List<ProductDTO> content = productPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        PagedResponseDTO<ProductDTO> pagedResponse = new PagedResponseDTO<>();
        pagedResponse.setContent(content);
        pagedResponse.setPage(PageMetadataDTO.from(productPage));

        return pagedResponse;
    }

    private Specification<Product> buildSpecification(ProductQuery query) {
        String name = query != null ? query.getName() : null;
        String description = query != null ? query.getDescription() : null;
        Long categoryId = query != null ? query.getCategoryId() : null;

        Specification<Product> specification = ProductSpecification.filterByCriteria(name, description);
        if (categoryId != null) {
            specification = specification.and((root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get("categoryId"), categoryId));
        }
        return specification;
    }

    private ProductQuery normalize(ProductQuery query) {
        return query != null ? query : ProductQuery.builder().build();
    }

    @Override
    @CachePut(value = CacheConfig.PRODUCTS_BY_ID, key = "#id")
    @CacheEvict(value = CacheConfig.PRODUCT_SEARCH, allEntries = true)
    public ProductDTO updateProduct(Long id, CreateProductRequestDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return persistProduct(productDTO, product);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.PRODUCTS_BY_ID, key = "#id"),
            @CacheEvict(value = CacheConfig.PRODUCT_SEARCH, allEntries = true)
    })
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    @NonNull
    private ProductDTO persistProduct(CreateProductRequestDTO productDTO, Product product) {
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());
        product.setCategoryId(productDTO.getCategoryId());

        Product saved = productRepository.save(product);
        return mapToDto(saved);
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

    public static String searchCacheKey(ProductQuery query) {
        ProductQuery normalized = query != null ? query : ProductQuery.builder().build();
        String nameKey = normalized.getName() != null ? normalized.getName() : "";
        String descriptionKey = normalized.getDescription() != null ? normalized.getDescription() : "";
        String categoryKey = normalized.getCategoryId() != null ? normalized.getCategoryId().toString() : "";

        return String.format("name:%s|desc:%s|cat:%s|page:%d|size:%d|sort:%s",
                nameKey,
                descriptionKey,
                categoryKey,
                normalized.currentPage(),
                normalized.currentSize(),
                normalized.sortOrDefault());
    }
}
