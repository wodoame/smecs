package com.smecs.service;

import com.smecs.dto.InventoryDTO;
import com.smecs.dto.PagedResponseDTO;
import org.springframework.data.domain.Pageable;

public interface InventoryService {
    InventoryDTO getInventoryByProductId(Long productId);
    InventoryDTO updateStock(Long productId, Integer quantity);
    PagedResponseDTO<InventoryDTO> getAllInventory(Pageable pageable);
}
