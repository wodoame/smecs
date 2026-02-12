package com.smecs.service.impl;

import com.smecs.dao.ProductDAO;
import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.entity.Product;
import com.smecs.service.ProductService;
import com.smecs.service.CacheService;
import com.smecs.service.SearchCacheService;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.util.PaginationUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductDAO productDAO;
    private final CacheService<ProductDTO, Long> productCacheService;
    private final SearchCacheService<ProductDTO> searchCacheService;

    @Autowired
    public ProductServiceImpl(ProductDAO productDAO, CacheService<ProductDTO, Long> productCacheService, SearchCacheService<ProductDTO> searchCacheService) {
        this.productDAO = productDAO;
        this.productCacheService = productCacheService;
        this.searchCacheService = searchCacheService;
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
    public PagedResponseDTO<ProductDTO> getProducts(String name, String description, Long categoryId, int page, int size, String sort) {
        // Check cache first - include categoryId in cache key
        String queryKey = buildCacheKey(name, categoryId);
        Optional<PagedResponseDTO<ProductDTO>> cached = searchCacheService.getSearchResults(queryKey, page, size, sort);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Cache miss - create Pageable and fetch from database
        Pageable pageable = PageRequest.of(page - 1, size, PaginationUtils.parseSort(sort, "id"));
        PagedResponseDTO<ProductDTO> results = getProducts(name, description, categoryId, pageable);

        // Store in cache
        searchCacheService.putSearchResults(queryKey, page, size, sort, results);

        return results;
    }

    @Override
    public PagedResponseDTO<ProductDTO> getProducts(String name, String description, Long categoryId, Pageable pageable) {
        // Use native SQL implementation from DAO for low-level database operations
        Page<Product> productPage = productDAO.searchProducts(name, description, categoryId, pageable);

        List<ProductDTO> content = productPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        PagedResponseDTO<ProductDTO> pagedResponse = new PagedResponseDTO<>();
        pagedResponse.setContent(content);
        pagedResponse.setPage(PageMetadataDTO.from(productPage));

        return pagedResponse;
    }

    /**
     * Builds a cache key that includes both the search query and category filter.
     * This ensures separate cache entries for different category filters.
     */
    private String buildCacheKey(String name, Long categoryId) {
        String baseKey = name != null ? name : "";
        if (categoryId != null) {
            return baseKey + "|cat:" + categoryId;
        }
        return baseKey;
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
