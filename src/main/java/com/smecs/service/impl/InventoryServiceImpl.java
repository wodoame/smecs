package com.smecs.service.impl;

import com.smecs.dto.*;
import com.smecs.entity.Category;
import com.smecs.entity.Inventory;
import com.smecs.entity.Product;
import com.smecs.repository.CategoryRepository;
import com.smecs.repository.InventoryRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.service.InventoryService;
import com.smecs.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    @Autowired
    public InventoryServiceImpl(InventoryRepository inventoryRepository,
                                ProductRepository productRepository,
                                ProductService productService,
                                CategoryRepository categoryRepository) {
        this.inventoryRepository = inventoryRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public InventoryDTO getInventoryByProductId(Long productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(createEmptyInventoryForProduct(productId));
        return mapToDTO(inventory);
    }

    private Inventory createEmptyInventoryForProduct(Long productId) {
        // Just return a virtual inventory item with 0 quantity if not found,
        // to avoid error when fetching inventory for a product that hasn't had stock set yet.
        // Check if product exists first
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        Inventory inventory = new Inventory();
        inventory.setProduct(product);
        inventory.setQuantity(0);
        return inventory;
    }

    @Override
    @Transactional
    public InventoryDTO updateStock(Long productId, Integer quantity) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElse(new Inventory());

        if (inventory.getProduct() == null) {
             Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
             inventory.setProduct(product);
        }

        inventory.setQuantity(quantity);
        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToDTO(savedInventory);
    }

    @Override
    public PagedResponseDTO<InventoryDTO> searchInventory(String query, Pageable pageable) {
        Page<Inventory> inventoryPage;
        if (query == null || query.isBlank()) {
            inventoryPage = inventoryRepository.findAll(pageable);
        } else {
            inventoryPage = inventoryRepository.findByProduct_NameContainingIgnoreCase(query, pageable);
        }
        return getInventoryDTOPagedResponseDTO(inventoryPage);
    }

    private PagedResponseDTO<InventoryDTO> getInventoryDTOPagedResponseDTO(Page<Inventory> inventoryPage) {
        List<InventoryDTO> content = inventoryPage.getContent().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

        PagedResponseDTO<InventoryDTO> pagedResponse = new PagedResponseDTO<>();
        PageMetadataDTO pageMetadata = new PageMetadataDTO();

        pageMetadata.setPage(inventoryPage.getNumber());
        pageMetadata.setSize(inventoryPage.getSize());
        pageMetadata.setTotalElements(inventoryPage.getTotalElements());
        pageMetadata.setTotalPages(inventoryPage.getTotalPages());
        pageMetadata.setFirst(inventoryPage.isFirst());
        pageMetadata.setLast(inventoryPage.isLast());
        pageMetadata.setEmpty(inventoryPage.isEmpty());
        pageMetadata.setHasNext(inventoryPage.hasNext());
        pageMetadata.setHasPrevious(inventoryPage.hasPrevious());

        pagedResponse.setContent(content);
        pagedResponse.setPage(pageMetadata);
        return pagedResponse;
    }

    @Override
    @Transactional
    public InventoryDTO createInventoryWithProduct(CreateInventoryRequestDTO request) {
        // Validate that category exists
        Long categoryId = request.getProduct().getCategoryId();
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }

        // Build ProductDTO from request
        ProductDTO productDTO = new ProductDTO();
        productDTO.setName(request.getProduct().getName());
        productDTO.setDescription(request.getProduct().getDescription());
        productDTO.setPrice(request.getProduct().getPrice());
        productDTO.setImageUrl(request.getProduct().getImageUrl());

        // Set category for product
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryId(categoryId.intValue());
        productDTO.setCategory(categoryDTO);

        // Create product using ProductService
        ProductDTO createdProduct = productService.createProduct(productDTO);

        // Create inventory for the new product
        Inventory inventory = new Inventory();
        Product product = productRepository.findById(createdProduct.getId())
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + createdProduct.getId()));
        inventory.setProduct(product);
        inventory.setQuantity(request.getQuantity());

        // Save inventory
        Inventory savedInventory = inventoryRepository.save(inventory);

        return mapToDTO(savedInventory);
    }

    private InventoryDTO mapToDTO(Inventory inventory) {
        InventoryDTO dto = new InventoryDTO();
        dto.setId(inventory.getId());
        dto.setQuantity(inventory.getQuantity());

        // Map Product to ProductDTO
        Product product = inventory.getProduct();
//        System.out.println("product_name = " + product.getName());
        if (product != null) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            productDTO.setDescription(product.getDescription());
            productDTO.setPrice(product.getPrice());
            productDTO.setImageUrl(product.getImageUrl());
            if (product.getCategory() != null) {
                Category category = product.getCategory();
                CategoryDTO categoryDTO = new CategoryDTO();
                categoryDTO.setCategoryId(category.getId().intValue());
                categoryDTO.setCategoryName(category.getName());
                categoryDTO.setDescription(category.getDescription());
                categoryDTO.setImageUrl(category.getImageUrl());
                productDTO.setCategory(categoryDTO);
            }
            dto.setProduct(productDTO);
        }
        return dto;
    }
}
