package com.smecs.service.impl;

import com.smecs.dao.InventoryDAO;
import com.smecs.dto.*;
import com.smecs.entity.Inventory;
import com.smecs.repository.CategoryRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.service.InventoryService;
import com.smecs.service.ProductService;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.util.PaginationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryDAO inventoryDAO;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final InventoryCacheService inventoryCacheService;

    @Autowired
    public InventoryServiceImpl(InventoryDAO inventoryDAO,
                                ProductRepository productRepository,
                                ProductService productService,
                                CategoryRepository categoryRepository,
                                InventoryCacheService inventoryCacheService) {
        this.inventoryDAO = inventoryDAO;
        this.productRepository = productRepository;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
        this.inventoryCacheService = inventoryCacheService;
    }

    @Override
    public InventoryDTO getInventoryById(Long id) {
        Inventory inventory = inventoryDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + id));
        return mapToDTO(inventory);
    }

    @Override
    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryDAO.findByProductId(productId)
                .orElse(createEmptyInventoryForProduct(productId));
        return mapToDTO(inventory);
    }

    private Inventory createEmptyInventoryForProduct(Long productId) {
        // Just return a virtual inventory item with 0 quantity if not found,
        // to avoid error when fetching inventory for a product that hasn't had stock set yet.
        // Check if product exists first
        if (!productRepository.existsById(productId)) {
             throw new ResourceNotFoundException("Product not found with id: " + productId);
        }

        Inventory inventory = new Inventory();
        inventory.setProductId(productId);
        inventory.setQuantity(0);
        return inventory;
    }


    @Override
    public PagedResponseDTO<InventoryDTO> searchInventory(String query, Pageable pageable) {
        // Delegate to DAO native SQL search
        Page<Inventory> inventoryPage = inventoryDAO.searchInventory(query, pageable);

        return getInventoryDTOPagedResponseDTO(inventoryPage);
    }

    @Override
    public PagedResponseDTO<InventoryDTO> searchInventory(String query, int page, int size, String sort) {
        Optional<PagedResponseDTO<InventoryDTO>> cached = inventoryCacheService.getSearchResults(query, page, size, sort);
        if (cached.isPresent()) {
            return cached.get();
        }

        Sort sortSpec = PaginationUtils.parseSort(sort, "id");
        Pageable pageable = PageRequest.of(page - 1, size, sortSpec);

        PagedResponseDTO<InventoryDTO> result = searchInventory(query, pageable);

        inventoryCacheService.putSearchResults(query, page, size, sort, result);

        return result;
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

    @Override
    @Transactional
    public InventoryDTO createInventory(CreateInventoryRequestDTO request) {
        Inventory inventory = new Inventory();

        // Check if creating inventory for existing product or new product
        if (request.getProductId() != null) {
            // Create inventory for existing product
            if (!productRepository.existsById(request.getProductId())) {
                throw new ResourceNotFoundException("Product not found with id: " + request.getProductId());
            }

            // Check if inventory already exists for this product
            if (inventoryDAO.findByProductId(request.getProductId()).isPresent()) {
                throw new RuntimeException("Inventory already exists for product id: " + request.getProductId());
            }

            inventory.setProductId(request.getProductId());
        } else if (request.getProduct() != null) {
            // Create new product and inventory together
            Long categoryId = request.getProduct().getCategoryId();
            if (categoryId != null && !categoryRepository.existsById(categoryId)) {
                throw new RuntimeException("Category not found with id: " + categoryId);
            }

            // Build CreateProductRequestDTO from request
            CreateProductRequestDTO createProductRequest = buildCreateProductRequestDTO(request, categoryId);

            // Create product using ProductService
            ProductDTO createdProduct = productService.createProduct(createProductRequest);
            inventory.setProductId(createdProduct.getId());
        } else {
            throw new RuntimeException("Either productId or product details must be provided");
        }

        // Set quantity and save inventory
        inventory.setQuantity(request.getQuantity());
        Inventory savedInventory = inventoryDAO.save(inventory);
        // Invalidate cache for this inventory and all search lists
        inventoryCacheService.invalidateById(savedInventory.getId());
        inventoryCacheService.invalidateAllList();
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
    public InventoryDTO updateInventory(Long inventoryId, UpdateInventoryRequestDTO request) {
        // Find the inventory
        Inventory inventory = inventoryDAO.findById(inventoryId)
                .orElseThrow(() -> new RuntimeException("Inventory not found with id: " + inventoryId));

        // Update inventory quantity
        inventory.setQuantity(request.getQuantity());
        Inventory savedInventory = inventoryDAO.save(inventory);
        // Invalidate cache for this inventory and all search lists
        inventoryCacheService.invalidateById(savedInventory.getId());
        inventoryCacheService.invalidateAllList();
        return mapToDTO(savedInventory);
    }

    @Override
    @Transactional
    public void deleteInventory(Long inventoryId) {
        if (!inventoryDAO.existsById(inventoryId)) {
            throw new ResourceNotFoundException("Inventory not found with id: " + inventoryId);
        }
        inventoryDAO.deleteById(inventoryId);
        // Invalidate cache for this inventory and all search lists
        inventoryCacheService.invalidateById(inventoryId);
        inventoryCacheService.invalidateAllList();
    }

    private InventoryDTO mapToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setProductId(inventory.getProductId());
        dto.setQuantity(inventory.getQuantity());
        return dto;
    }
}
