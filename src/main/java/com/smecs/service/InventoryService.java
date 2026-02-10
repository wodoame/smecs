package com.smecs.service;

import com.smecs.dto.CreateInventoryRequestDTO;
import com.smecs.dto.InventoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.UpdateInventoryRequestDTO;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    InventoryDTO getInventoryById(Long id);
    InventoryDTO getInventoryByProductId(Long productId);
    PagedResponseDTO<InventoryDTO> searchInventory(Pageable pageable);
    InventoryDTO createInventory(CreateInventoryRequestDTO request);
    InventoryDTO updateInventory(Long inventoryId, UpdateInventoryRequestDTO request);
    void deleteInventory(Long inventoryId);
}
