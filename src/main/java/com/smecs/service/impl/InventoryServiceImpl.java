package com.smecs.service.impl;

import com.smecs.config.CacheConfig;
import com.smecs.dto.CreateInventoryRequestDTO;
import com.smecs.dto.CreateProductRequestDTO;
import com.smecs.dto.InventoryDTO;
import com.smecs.dto.InventoryQuery;
import com.smecs.dto.PageMetadataDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ProductDTO;
import com.smecs.dto.UpdateInventoryRequestDTO;
import com.smecs.entity.Inventory;
import com.smecs.entity.Product;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.CategoryRepository;
import com.smecs.repository.InventoryRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.service.InventoryService;
import com.smecs.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor_ = @Autowired)
@Service
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @Override
    @Cacheable(value = CacheConfig.INVENTORIES_BY_ID, key = "#id")
    public InventoryDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        return mapToDTO(inventory);
    }

    @Override
    @Cacheable(value = CacheConfig.INVENTORIES_BY_PRODUCT_ID, key = "#productId")
    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProduct_Id(productId)
                .orElse(createEmptyInventoryForProduct(productId));
        return mapToDTO(inventory);
    }

    private Inventory createEmptyInventoryForProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(0);
        return inventory;
    }

    @Override
    @Cacheable(value = CacheConfig.INVENTORY_SEARCH,
            key = "T(com.smecs.service.impl.InventoryServiceImpl).searchCacheKey(#query)")
    public PagedResponseDTO<InventoryDTO> searchInventory(InventoryQuery query) {
        String searchTerm = Optional.ofNullable(query).map(InventoryQuery::getQuery).orElse("");
        int page = Optional.ofNullable(query).map(InventoryQuery::getPage).orElse(1);
        int size = Optional.ofNullable(query).map(InventoryQuery::getSize).orElse(10);
        String sort = Optional.ofNullable(query).map(InventoryQuery::getSort).orElse("id,asc");

        Pageable pageable = buildPageable(sort, page, size);
        Page<Inventory> inventoryPage = inventoryRepository.searchInventory(searchTerm, pageable);
        return getInventoryDTOPagedResponseDTO(inventoryPage);
    }

    @Override
    @Transactional
    @Caching(put = {
            @CachePut(value = CacheConfig.INVENTORIES_BY_ID, key = "#result.id"),
            @CachePut(value = CacheConfig.INVENTORIES_BY_PRODUCT_ID, key = "#result.productId")
    }, evict = {
            @CacheEvict(value = CacheConfig.INVENTORY_SEARCH, allEntries = true)
    })
    public InventoryDTO createInventory(CreateInventoryRequestDTO request) {
        Inventory inventory = new Inventory();

        if (request.getProductId() != null) {
            Product product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + request.getProductId()));

            if (inventoryRepository.findByProduct_Id(request.getProductId()).isPresent()) {
                throw new RuntimeException("Inventory already exists for product id: " + request.getProductId());
            }

            inventory.setProduct(product);
        } else if (request.getProduct() != null) {
            Long categoryId = request.getProduct().getCategoryId();
            if (categoryId != null && !categoryRepository.existsById(categoryId)) {
                throw new RuntimeException("Category not found with id: " + categoryId);
            }

            CreateProductRequestDTO createProductRequest = buildCreateProductRequestDTO(request, categoryId);
            ProductDTO createdProduct = productService.createProduct(createProductRequest);
            Product product = productRepository.findById(createdProduct.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product not found with id: " + createdProduct.getId()));
            inventory.setProduct(product);
        } else {
            throw new RuntimeException("Either productId or product details must be provided");
        }

        inventory.setQuantity(request.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToDTO(savedInventory);
    }

    private static CreateProductRequestDTO buildCreateProductRequestDTO(CreateInventoryRequestDTO request, Long categoryId) {
        CreateProductRequestDTO dto = new CreateProductRequestDTO();
        dto.setName(request.getProduct().getName());
        dto.setDescription(request.getProduct().getDescription());
        dto.setPrice(request.getProduct().getPrice());
        dto.setImageUrl(request.getProduct().getImageUrl());
        dto.setCategoryId(categoryId);
        return dto;
    }

    @Override
    @Transactional
    @Caching(put = {
            @CachePut(value = CacheConfig.INVENTORIES_BY_ID, key = "#result.id"),
            @CachePut(value = CacheConfig.INVENTORIES_BY_PRODUCT_ID, key = "#result.productId")
    }, evict = {
            @CacheEvict(value = CacheConfig.INVENTORY_SEARCH, allEntries = true)
    })
    public InventoryDTO updateInventory(Long inventoryId, UpdateInventoryRequestDTO request) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found with id: " + inventoryId));

        inventory.setQuantity(request.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToDTO(savedInventory);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = CacheConfig.INVENTORIES_BY_ID, key = "#inventoryId"),
            @CacheEvict(value = CacheConfig.INVENTORIES_BY_PRODUCT_ID, allEntries = true),
            @CacheEvict(value = CacheConfig.INVENTORY_SEARCH, allEntries = true)
    })
    public void deleteInventory(Long inventoryId) {
        if (!inventoryRepository.existsById(inventoryId)) {
            throw new ResourceNotFoundException("Inventory not found with id: " + inventoryId);
        }
        inventoryRepository.deleteById(inventoryId);
    }

    private InventoryDTO mapToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProduct() != null ? inventory.getProduct().getId() : null);
        dto.setQuantity(inventory.getQuantity());
        return dto;
    }

    private PagedResponseDTO<InventoryDTO> getInventoryDTOPagedResponseDTO(Page<Inventory> inventoryPage) {
        List<InventoryDTO> content = inventoryPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        PagedResponseDTO<InventoryDTO> pagedResponse = new PagedResponseDTO<>();
        pagedResponse.setContent(content);
        pagedResponse.setPage(PageMetadataDTO.from(inventoryPage));
        return pagedResponse;
    }

    private Pageable buildPageable(String sortClause, int page, int size) {
        int pageIndex = Math.max(0, page - 1);
        int pageSize = Math.max(1, size);
        String sortField = "id";
        Sort.Direction direction = Sort.Direction.ASC;

        if (sortClause != null && !sortClause.isBlank()) {
            String[] sortParams = sortClause.split(",");
            if (sortParams.length > 0 && !sortParams[0].isBlank()) {
                sortField = sortParams[0];
            }
            if (sortParams.length > 1 && !sortParams[1].isBlank()) {
                direction = sortParams[1].equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
            }
        }

        return PageRequest.of(pageIndex, pageSize, Sort.by(direction, sortField));
    }

    public static String searchCacheKey(InventoryQuery query) {
        String searchTerm = Optional.ofNullable(query).map(InventoryQuery::getQuery).orElse("");
        int page = Optional.ofNullable(query).map(InventoryQuery::getPage).orElse(1);
        int size = Optional.ofNullable(query).map(InventoryQuery::getSize).orElse(10);
        String sort = Optional.ofNullable(query).map(InventoryQuery::getSort).orElse("id,asc");
        return String.format("%s|%d|%d|%s", searchTerm, page, size, sort);
    }
}
