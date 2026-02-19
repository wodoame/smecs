package com.smecs.service.impl;

import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ProductQuery;
import com.smecs.entity.Product;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.ProductRepository;
import com.smecs.repository.ProductSpecification;
import com.smecs.service.CacheService;
import com.smecs.service.ProductService;
import com.smecs.service.SearchCacheService;
import com.smecs.util.PaginationUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final CacheService<ProductDTO, Long> productCacheService;
    private final SearchCacheService<ProductDTO> searchCacheService;

    @Autowired
    public ProductServiceImpl(ProductRepository productRepository,
                              CacheService<ProductDTO, Long> productCacheService,
                              SearchCacheService<ProductDTO> searchCacheService) {
        this.productRepository = productRepository;
        this.productCacheService = productCacheService;
        this.searchCacheService = searchCacheService;
    }

    @Override
    public ProductDTO createProduct(CreateProductRequestDTO request) {
        Product product = new Product();
        return persistProduct(request, product);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        return productCacheService.getById(id)
                .orElseGet(() -> loadProduct(id));
    }

    @Override
    public PagedResponseDTO<ProductDTO> getProducts(ProductQuery query) {
        ProductQuery normalized = normalize(query);
        String queryKey = buildCacheKey(normalized);
        int page = normalized.currentPage();
        int size = normalized.currentSize();
        String sort = normalized.sortOrDefault();

        Optional<PagedResponseDTO<ProductDTO>> cached = searchCacheService.getSearchResults(queryKey, page, size, sort);
        if (cached.isPresent()) {
            return cached.get();
        }

        Sort sortSpec = PaginationUtils.parseSort(sort, "id");
        Pageable pageable = PageRequest.of(page - 1, size, sortSpec);
        PagedResponseDTO<ProductDTO> results = getProducts(normalized, pageable);
        searchCacheService.putSearchResults(queryKey, page, size, sort, results);
        return results;
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

    private String buildCacheKey(ProductQuery query) {
        String baseKey = query != null && query.getName() != null ? query.getName() : "";
        Long categoryId = query != null ? query.getCategoryId() : null;
        if (categoryId != null) {
            return baseKey + "|cat:" + categoryId;
        }
        return baseKey;
    }

    private ProductQuery normalize(ProductQuery query) {
        return query != null ? query : ProductQuery.builder().build();
    }

    @Override
    public ProductDTO updateProduct(Long id, CreateProductRequestDTO productDTO) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return persistProduct(productDTO, product);
    }

    @NonNull
    private ProductDTO persistProduct(CreateProductRequestDTO productDTO, Product product) {
        product.setName(productDTO.getName());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setImageUrl(productDTO.getImageUrl());
        product.setCategoryId(productDTO.getCategoryId());

        Product saved = productRepository.save(product);
        ProductDTO result = mapToDto(saved);
        productCacheService.put(result);
        productCacheService.invalidateAllList();
        return result;
    }

    @Override
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
        productCacheService.invalidateById(id);
        productCacheService.invalidateAllList();
    }

    private ProductDTO loadProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        ProductDTO dto = mapToDto(product);
        productCacheService.put(dto);
        return dto;
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
