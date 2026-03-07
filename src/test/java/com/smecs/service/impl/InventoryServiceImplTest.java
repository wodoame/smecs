package com.smecs.service.impl;

import com.smecs.dto.CreateInventoryRequestDTO;
import com.smecs.dto.InventoryDTO;
import com.smecs.entity.Inventory;
import com.smecs.entity.Product;
import com.smecs.exception.ResourceNotFoundException;
import com.smecs.repository.CategoryRepository;
import com.smecs.repository.InventoryRepository;
import com.smecs.repository.ProductRepository;
import com.smecs.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceImplTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private InventoryServiceImpl inventoryService;

    @Test
    void getInventoryByProductId_returnsEmptyInventoryWhenMissing() {
        Product product = new Product();
        product.setId(5L);
        when(inventoryRepository.findByProduct_Id(5L)).thenReturn(Optional.empty());
        when(productRepository.findById(5L)).thenReturn(Optional.of(product));

        InventoryDTO result = inventoryService.getInventoryByProductId(5L);

        assertThat(result.getProductId()).isEqualTo(5L);
        assertThat(result.getQuantity()).isEqualTo(0);
    }

    @Test
    void createInventory_withProductIdPersistsInventory() {
        CreateInventoryRequestDTO request = new CreateInventoryRequestDTO();
        request.setProductId(7L);
        request.setQuantity(12);

        Product product = new Product();
        product.setId(7L);
        when(productRepository.findById(7L)).thenReturn(Optional.of(product));
        when(inventoryRepository.findByProduct_Id(7L)).thenReturn(Optional.empty());
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(invocation -> {
            Inventory inventory = invocation.getArgument(0);
            inventory.setId(3L);
            return inventory;
        });

        InventoryDTO result = inventoryService.createInventory(request);

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getProductId()).isEqualTo(7L);
        assertThat(result.getQuantity()).isEqualTo(12);
    }

    @Test
    void getInventoryById_throwsWhenMissing() {
        when(inventoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> inventoryService.getInventoryById(99L));
    }
}

