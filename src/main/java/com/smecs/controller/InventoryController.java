package com.smecs.controller;

import com.smecs.dto.CreateInventoryRequestDTO;
import com.smecs.dto.InventoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseDTO<InventoryDTO> createInventoryWithProduct(@Valid @RequestBody CreateInventoryRequestDTO request) {
        return new ResponseDTO<>("success", "Inventory and product created", inventoryService.createInventoryWithProduct(request));
    }

    @GetMapping
    public ResponseDTO<PagedResponseDTO<InventoryDTO>> getAllInventory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Sort sortSpec = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortSpec);
        return new ResponseDTO<>("success", "Inventory retrieved", inventoryService.getAllInventory(pageable));
    }

    @GetMapping("/product/{productId}")
    public ResponseDTO<InventoryDTO> getInventoryByProductId(@PathVariable Long productId) {
        return new ResponseDTO<>("success", "Inventory found", inventoryService.getInventoryByProductId(productId));
    }

    @PutMapping("/product/{productId}")
    public ResponseDTO<InventoryDTO> updateStock(@PathVariable Long productId, @RequestBody Integer quantity) {
        return new ResponseDTO<>("success", "Stock updated", inventoryService.updateStock(productId, quantity));
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "id");
        }
        String[] parts = sort.split(",");
        String property = parts[0].trim();
        Sort.Direction direction = parts.length > 1 ? Sort.Direction.fromString(parts[1].trim()) : Sort.Direction.ASC;
        return Sort.by(direction, property);
    }
}
