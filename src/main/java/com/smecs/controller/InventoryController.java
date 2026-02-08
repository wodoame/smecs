package com.smecs.controller;

import com.smecs.dto.CreateInventoryRequestDTO;
import com.smecs.dto.InventoryDTO;
import com.smecs.dto.PagedResponseDTO;
import com.smecs.dto.ResponseDTO;
import com.smecs.dto.UpdateInventoryRequestDTO;
import com.smecs.service.InventoryService;
import com.smecs.service.impl.InventoryCacheService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Validated
@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

    private final InventoryService inventoryService;
    private final InventoryCacheService inventoryCacheService;

    @Autowired
    public InventoryController(InventoryService inventoryService, InventoryCacheService inventoryCacheService) {
        this.inventoryService = inventoryService;
        this.inventoryCacheService = inventoryCacheService;
    }

    @PostMapping
    public ResponseEntity<ResponseDTO<InventoryDTO>> createInventory(@Valid @RequestBody CreateInventoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ResponseDTO<>("success", "Inventory created", inventoryService.createInventory(request)));
    }

    @GetMapping
    public ResponseEntity<ResponseDTO<PagedResponseDTO<InventoryDTO>>> searchInventory(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "id,asc") String sort
    ) {
        Optional<PagedResponseDTO<InventoryDTO>> cached = inventoryCacheService.getSearchResults(query, page, size, sort);
        if (cached.isPresent()) {
            return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory retrieved", cached.get()));
        }

        Sort sortSpec = parseSort(sort);
        Pageable pageable = PageRequest.of(page - 1, size, sortSpec);

        PagedResponseDTO<InventoryDTO> result = inventoryService.searchInventory(query, pageable);
        inventoryCacheService.putSearchResults(query, page, size, sort, result);

        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory retrieved", result));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ResponseDTO<InventoryDTO>> getInventoryByProductId(@PathVariable Long productId) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory found", inventoryService.getInventoryByProductId(productId)));
    }


    @PutMapping("/{inventoryId}")
    public ResponseEntity<ResponseDTO<InventoryDTO>> updateInventory(
            @PathVariable Long inventoryId,
            @Valid @RequestBody UpdateInventoryRequestDTO request) {
        return ResponseEntity.ok(new ResponseDTO<>("success", "Inventory updated",
                inventoryService.updateInventory(inventoryId, request)));
    }

    @DeleteMapping("/{inventoryId}")
    public ResponseEntity<Void> deleteInventory(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId);
        return ResponseEntity.noContent().build();
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
